/*
 * $Revision$
 * $Date$
 *
 * Copyright (C) 1999-$year$ Jive Software. All rights reserved.
 *
 * This software is the proprietary information of Jive Software. Use is subject to license terms.
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