/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.model.impl.data.transformers;

import org.jkiss.code.NotNull;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBPDataKind;
import org.jkiss.dbeaver.model.DBValueFormatting;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.data.DBDAttributeTransformer;
import org.jkiss.dbeaver.model.data.DBDDisplayFormat;
import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCSession;
import org.jkiss.dbeaver.model.impl.data.ProxyValueHandler;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.utils.CommonUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Transforms numeric attribute value into string in a specified radix
 */
public class RadixAttributeTransformer implements DBDAttributeTransformer {

    private static final Log log = Log.getLog(RadixAttributeTransformer.class);

    public static final String PROP_RADIX = "radix";
    public static final String PROP_BITS = "bits";
    public static final String PROP_PREFIX = "prefix";
    public static final String PROP_UNSIGNED = "unsigned";

    public static final String PREFIX_HEX = "0x";
    public static final String PREFIX_OCT = "0";
    public static final String PREFIX_BIN = "0b";

    @Override
    public void transformAttribute(@NotNull DBCSession session, @NotNull DBDAttributeBinding attribute, @NotNull List<Object[]> rows, @NotNull Map<String, Object> options) throws DBException {
        int radix = 16;
        int bits = 32;
        boolean showPrefix = false;
        boolean unsigned = false;
        if (options.containsKey(PROP_RADIX)) {
            radix = CommonUtils.toInt(options.get(PROP_RADIX), radix);
        }
        if (options.containsKey(PROP_BITS)) {
            bits = CommonUtils.toInt(options.get(PROP_BITS), bits);
        }
        if (options.containsKey(PROP_PREFIX)) {
            showPrefix = CommonUtils.getBoolean(options.get(PROP_PREFIX), showPrefix);
        }
        if (options.containsKey(PROP_UNSIGNED)) {
            unsigned = CommonUtils.getBoolean(options.get(PROP_UNSIGNED), unsigned);
        }

        attribute.setTransformHandler(new RadixValueHandler(attribute.getValueHandler(), radix, bits, showPrefix, unsigned));
        attribute.setPresentationAttribute(
            new TransformerPresentationAttribute(attribute, "StringNumber", -1, DBPDataKind.STRING));
    }

    private static class RadixValueHandler extends ProxyValueHandler {
        private final int radix;
        private final int bits;
        private final boolean showPrefix;
        private final boolean unsigned;

        public RadixValueHandler(DBDValueHandler target, int radix, int bits, boolean showPrefix, boolean unsigned) {
            super(target);
            this.radix = radix;
            this.bits = bits;
            this.showPrefix = showPrefix;
            this.unsigned = unsigned;
        }

        @NotNull
        @Override
        public String getValueDisplayString(@NotNull DBSTypedObject column, @Nullable Object value, @NotNull DBDDisplayFormat format) {
            if (value instanceof Number) {
                final long longValue = ((Number) value).longValue();
                final String strValue;
                final StringBuilder sb = new StringBuilder();
                if (unsigned || longValue >= 0) {
                    strValue = Long.toUnsignedString(longValue, radix).toUpperCase(Locale.ENGLISH);
                } else {
                    strValue = Long.toString(longValue, radix).substring(1).toUpperCase(Locale.ENGLISH);
                    sb.append("-");
                }
                if (showPrefix) {
                    if (radix == 16) {
                        sb.append(PREFIX_HEX);
                    } else if (radix == 8) {
                        sb.append(PREFIX_OCT);
                    } else if (radix == 2) {
                        sb.append(PREFIX_BIN);
                    }
                }
                if (radix == 2) {
                    sb.append(strValue, 0, CommonUtils.clamp(strValue.length(), 1, bits));
                } else {
                    sb.append(strValue);
                }
                return sb.toString();
            }
            return DBValueFormatting.getDefaultValueDisplayString(value, format);
        }

        @Nullable
        @Override
        public Object getValueFromObject(@NotNull DBCSession session, @NotNull DBSTypedObject type, @Nullable Object object, boolean copy, boolean validateValue) throws DBCException {
            if (object instanceof String) {
                String strValue = (String) object;
                String strValueSign = "";
                if (strValue.isEmpty()) {
                    return 0;
                }
                if (strValue.charAt(0) == '-') {
                    strValue = strValue.substring(1);
                    strValueSign = "-";
                }
                if (showPrefix) {
                    if (radix == 16 && strValue.startsWith(PREFIX_HEX)) {
                        strValue = strValue.substring(2);
                    } else if (radix == 8 && strValue.startsWith(PREFIX_OCT)) {
                        strValue = strValue.substring(1);
                    } else if (radix == 2 && strValue.startsWith(PREFIX_BIN)) {
                        strValue = strValue.substring(2);
                    }
                }
                try {
                    return Long.parseLong(strValueSign + strValue, radix);
                } catch (NumberFormatException e) {
                    log.debug(e);
                }
            }
            return super.getValueFromObject(session, type, object, copy, validateValue);
        }
    }
}
