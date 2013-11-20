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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.jive.utils.http.client.rest.RequestHelper;
import com.jivesoftware.os.kensaku.shared.KensakuQuery;
import com.jivesoftware.os.kensaku.shared.KensakuResult;
import com.jivesoftware.os.kensaku.shared.KensakuResults;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;

public class JSearch extends JFrame {

    private static final ObjectMapper mapper = new ObjectMapper();
    RequestHelper requestHelper;
    JPanel viewResults;
    JTextField tenant;
    JTextField query;

    public JSearch(RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        initComponents();
    }

    private void initComponents() {

        viewResults = new JPanel(new SpringLayout());
        viewResults.setPreferredSize(new Dimension(800, 400));

        JPanel m = new JPanel(new SpringLayout());

        m.add(new JLabel("tenant"));
        tenant = new JTextField("sony", 120);
        tenant.setMinimumSize(new Dimension(120, 24));
        tenant.setMaximumSize(new Dimension(120, 24));
        m.add(tenant);

        m.add(new JLabel("query"));
        query = new JTextField("*", 120);
        query.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        });
        query.setMinimumSize(new Dimension(120, 24));
        query.setMaximumSize(new Dimension(120, 24));
        m.add(query);

        m.add(new JLabel(""));

        JButton refresh = new JButton("Search", Util.icon("refresh"));
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        refresh();
                    }
                });
            }
        });
        m.add(refresh);

        SpringUtils.makeCompactGrid(m, 3, 2, 24, 24, 16, 16);

        JScrollPane scrollRoutes = new JScrollPane(viewResults,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollRoutes.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        setLayout(new BorderLayout(8, 8));
        add(m, BorderLayout.NORTH);
        add(scrollRoutes, BorderLayout.CENTER);
        pack();

    }

    public void refresh() {

        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("body", query.getText());
            byte[] query = mapper.writeValueAsBytes(fields);

            KensakuQuery kensakuQuery = new KensakuQuery(tenant.getText(), 0, 10, query);
            KensakuResults kensakuResults = requestHelper.executeRequest(kensakuQuery,
                "/kensaku/search", KensakuResults.class, null);
            System.out.println("Search:" + kensakuResults);
            if (kensakuResults != null) {
                viewResults.removeAll();
                int count = 0;

                for (KensakuResult r : kensakuResults.results) {
                    viewResults.add(new JLabel(r.toString()));
                    count++;
                }
                SpringUtils.makeCompactGrid(viewResults, count, 1, 0, 0, 0, 0);
                viewResults.revalidate();
                viewResults.repaint();
            } else {
                viewResults.removeAll();
                viewResults.add(new JLabel("No results"));
                viewResults.revalidate();
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
        viewResults.getParent().revalidate();
        viewResults.getParent().repaint();
    }
}