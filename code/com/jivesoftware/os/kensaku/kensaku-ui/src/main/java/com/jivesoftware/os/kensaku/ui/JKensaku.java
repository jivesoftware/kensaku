package com.jivesoftware.os.kensaku.ui;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jivesoftware.os.jive.utils.http.client.HttpClient;
import com.jivesoftware.os.jive.utils.http.client.HttpClientConfig;
import com.jivesoftware.os.jive.utils.http.client.HttpClientConfiguration;
import com.jivesoftware.os.jive.utils.http.client.HttpClientFactory;
import com.jivesoftware.os.jive.utils.http.client.HttpClientFactoryProvider;
import com.jivesoftware.os.jive.utils.http.client.rest.RequestHelper;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

public class JKensaku extends javax.swing.JFrame {

    public JKensaku() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Kensaku - (japanese 'search' pron. ke N sA Ku)");

        JPanel connectTo = new JPanel();
        connectTo.setLayout(new BoxLayout(connectTo, BoxLayout.X_AXIS));
        connectTo.add(new JLabel("host:"));
        final JTextField editHost = new JTextField("localhost", 120);
        editHost.setMinimumSize(new Dimension(120, 24));
        editHost.setMaximumSize(new Dimension(120, 24));
        connectTo.add(editHost);
        connectTo.add(Box.createRigidArea(new Dimension(10, 0)));
        connectTo.add(new JLabel("port:"));
        final JTextField editPort = new JTextField("8080", 48);
        editPort.setMinimumSize(new Dimension(120, 24));
        editPort.setMaximumSize(new Dimension(120, 24));
        connectTo.add(editPort);
        connectTo.setOpaque(true);
        connectTo.setBackground(Color.white);

        JPanel v = new JPanel(new SpringLayout());

        final ImageIcon background = Util.icon("cluster");
        JLabel banner = new JLabel(background);
        banner.setPreferredSize(new Dimension(400, 48));
        v.add(banner);
        v.add(connectTo);

        JButton index = new JButton("Index");
        index.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String key = editHost.getText() + ":" + editPort.getText();
                        RequestHelper buildHelper = buildHelper(editHost.getText(), editPort.getText());
                        JIndex v = new JIndex(buildHelper);
                        v.setTitle("Index " + editHost.getText() + ":" + editPort.getText());
                        v.setSize(800, 600);
                        v.setLocationRelativeTo(null);
                        v.setVisible(true);
                    }
                });
            }
        });

        v.add(index);

        JButton search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String key = editHost.getText() + ":" + editPort.getText();
                        RequestHelper buildHelper = buildHelper(editHost.getText(), editPort.getText());
                        JSearch v = new JSearch(buildHelper);
                        v.setTitle("Search " + editHost.getText() + ":" + editPort.getText());
                        v.setSize(800, 600);
                        v.setLocationRelativeTo(null);
                        v.setVisible(true);
                    }
                });
            }
        });

        v.add(search);

        JButton amza = new JButton("Amza");
        amza.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        String key = editHost.getText() + ":" + editPort.getText();
                        RequestHelper buildHelper = buildHelper(editHost.getText(), editPort.getText());
                        JAmza v = new JAmza(buildHelper);
                        v.setTitle("Amza " + editHost.getText() + ":" + editPort.getText());
                        v.setSize(800, 600);
                        v.setLocationRelativeTo(null);
                        v.setVisible(true);
                    }
                });
            }
        });

        v.add(amza);

        v.setOpaque(false);
        SpringUtils.makeCompactGrid(v, 5, 1, 10, 10, 10, 10);

        add(v);
        setPreferredSize(new Dimension(400, 300));
        setMaximumSize(new Dimension(400, 300));
        pack();
    }

    RequestHelper buildHelper(String host, String port) {

        HttpClientConfig httpClientConfig = HttpClientConfig.newBuilder().build();
        HttpClientFactory httpClientFactory = new HttpClientFactoryProvider()
                .createHttpClientFactory(Arrays.<HttpClientConfiguration>asList(httpClientConfig));
        HttpClient httpClient = httpClientFactory.createClient(host, Integer.parseInt(port));
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return new RequestHelper(httpClient, mapper);
    }

}