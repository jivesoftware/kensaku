/*
 * Copyright 2013 Jive Software, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.jivesoftware.os.kensaku.ui;

import java.io.IOException;
import javax.swing.ImageIcon;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 */
public class LaunchUI {

    public static void main(String[] args) throws IOException {
        Logger.getRootLogger().setLevel(Level.OFF);

        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Upena");

        final JKensaku kensaku = new JKensaku();
        ImageIcon icon = Util.icon("cluster");
        if (icon != null) {
            kensaku.setIconImage(icon.getImage());
        }

        /* Create and display the form */
        Util.invokeLater(new Runnable() {
            @Override
            public void run() {
                kensaku.setVisible(true);
            }
        });
    }
}