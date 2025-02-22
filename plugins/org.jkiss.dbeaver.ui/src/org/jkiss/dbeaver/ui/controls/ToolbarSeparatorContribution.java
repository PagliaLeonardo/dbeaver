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
package org.jkiss.dbeaver.ui.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.jkiss.dbeaver.ui.DBeaverIcons;
import org.jkiss.dbeaver.ui.UIIcon;

/**
 * ToolbarSeparatorContribution
 */
public class ToolbarSeparatorContribution extends WorkbenchWindowControlContribution {
    
    public static class Horizontal extends ToolbarSeparatorContribution {
        public Horizontal() {
            super(false);
        }
    }
    
    public static class Vertical extends ToolbarSeparatorContribution {
        public Vertical() {
            super(true);
        }
    }
    
    private boolean vertical;

    public ToolbarSeparatorContribution(boolean vertical)
    {
        super();
        this.vertical = vertical;
    }

    @Override
    protected Control createControl(Composite parent)
    {
        Label label = new Label(parent, SWT.NONE);
        label.setImage(DBeaverIcons.getImage(vertical ? UIIcon.SEPARATOR_V : UIIcon.SEPARATOR_H));
        return label;
    }
}