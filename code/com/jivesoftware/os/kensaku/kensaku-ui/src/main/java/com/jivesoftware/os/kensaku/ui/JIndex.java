package com.jivesoftware.os.kensaku.ui;

import com.jivesoftware.os.jive.utils.http.client.rest.RequestHelper;
import com.jivesoftware.os.kensaku.shared.KensakuDocument;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.EtchedBorder;
import org.apache.commons.io.FileUtils;

public class JIndex extends JFrame {

    RequestHelper requestHelper;
    JPanel viewResults;
    JTextField tenant;
    JTextField docId;
    JTextField directoryName;
    JTextArea body;

    public JIndex(RequestHelper requestHelper) {
        this.requestHelper = requestHelper;
        initComponents();
    }

    private void initComponents() {

        viewResults = new JPanel(new SpringLayout());
        viewResults.setPreferredSize(new Dimension(800, 400));

        JPanel input = new JPanel(new SpringLayout());

        input.add(new JLabel("tenant"));
        input.add(new JLabel("docId"));

        tenant = new JTextField("sony", 120);
        tenant.setMinimumSize(new Dimension(120, 24));
        tenant.setMaximumSize(new Dimension(120, 24));
        input.add(tenant);

        docId = new JTextField("1", 120);
        docId.setMinimumSize(new Dimension(120, 24));
        docId.setMaximumSize(new Dimension(120, 24));
        input.add(docId);

        directoryName = new JTextField("/", 120);
        directoryName.setMinimumSize(new Dimension(120, 24));
        directoryName.setMaximumSize(new Dimension(120, 24));

        SpringUtils.makeCompactGrid(input, 2, 2, 24, 24, 16, 16);

        body = new JTextArea();
        JScrollPane scrollBody = new JScrollPane(body,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollBody.setSize(new Dimension(800, 600));
        scrollBody.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        JButton index = new JButton("Save");
        index.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        index();
                    }
                });
            }
        });

        final JButton indexDirectory = new JButton("Index Directory");
        indexDirectory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Util.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            File d = new File(directoryName.getText());
                            System.out.println("Indexing:" + d);
                            index(d);
                        } catch (Exception x) {
                            x.printStackTrace();
                        }
                        //Create a file chooser
//                        final JFileChooser fc = new JFileChooser();
//                        System.out.println("A");
//                        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
//                        System.out.println("B");
//                        int returnVal = fc.showOpenDialog(indexDirectory);
//                        System.out.println("C " + returnVal);
//                        if (returnVal == JFileChooser.APPROVE_OPTION) {
//                            final File file = fc.getSelectedFile();
//                            System.out.println("Indexing:" + file);
//                            Util.invokeLater(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        index(file);
//                                    } catch (Exception x) {
//                                        x.printStackTrace();
//                                    }
//                                }
//                            });
//                        }
                    }
                });
            }
        });

        setLayout(new BorderLayout(8, 8));
        add(input, BorderLayout.NORTH);
        add(scrollBody, BorderLayout.CENTER);

        JPanel id = new JPanel(new BorderLayout(10, 10));
        id.add(directoryName, BorderLayout.CENTER);
        id.add(indexDirectory, BorderLayout.EAST);

        JPanel buttons = new JPanel(new BorderLayout(10, 10));
        buttons.add(index, BorderLayout.NORTH);
        buttons.add(id, BorderLayout.SOUTH);

        add(buttons, BorderLayout.SOUTH);
        pack();
    }

    public void index() {

        Map<String, List<String>> fields = new HashMap<>();
        fields.put("body", Arrays.asList(body.getText()));
        long did = Long.parseLong(docId.getText());
        KensakuDocument document = new KensakuDocument(tenant.getText(), did, System.currentTimeMillis(), fields);
        String response = requestHelper.executeRequest(Arrays.asList(document),
            "/kensaku/add", String.class, null);

        System.out.println("Indexed: " + response + " " + document);
        if (response != null) {
            docId.setText(Long.toString(did + 1));
            viewResults.removeAll();
            viewResults.add(new JLabel(response));
            viewResults.revalidate();
            viewResults.repaint();
        } else {
            viewResults.removeAll();
            viewResults.add(new JLabel("No results"));
            viewResults.revalidate();
        }
        if (viewResults.getParent() != null) {
            viewResults.getParent().revalidate();
            viewResults.getParent().repaint();
        }
    }

    public void index(File file) throws IOException {
        if (file == null) {
            return;
        }
        if (file.isDirectory()) {
            System.out.println("Listing files..." + file);
            for (File sourceFile : FileUtils.listFiles(file, new String[]{ "java" }, true)) {
                indexFile(sourceFile);
            }
        } else {
            indexFile(file);
        }
    }

    private void indexFile(File sourceFile) throws NumberFormatException, IOException {
        Map<String, List<String>> fields = new HashMap<>();
        fields.put("body", Arrays.asList(FileUtils.readFileToString(sourceFile)));
        long did = Long.parseLong(docId.getText());
        KensakuDocument document = new KensakuDocument(tenant.getText(), did, System.currentTimeMillis(), fields);
        String response = requestHelper.executeRequest(Arrays.asList(document),
            "/kensaku/add", String.class, null);
        if (response != null) {
            docId.setText(Long.toString(did + 1));
            System.out.println("Indexed: " + sourceFile);
        }
    }
}
