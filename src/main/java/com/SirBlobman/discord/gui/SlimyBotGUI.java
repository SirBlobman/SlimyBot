package com.SirBlobman.discord.gui;

import com.SirBlobman.discord.SlimyBot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.lang.Validate;

public class SlimyBotGUI {
    public SlimyBotGUI() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        JLabel outputLabel = new JLabel("  Output");
        frame.add(outputLabel, BorderLayout.NORTH);

        JTextArea consoleOutput = new JTextArea();
        TextAreaOutputStream textOutput = new TextAreaOutputStream(System.out, consoleOutput);
        PrintStream printStream = new PrintStream(textOutput);
        System.setOut(printStream); System.setErr(printStream);
        
        Font consoleFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);
        consoleOutput.setFont(consoleFont);
        consoleOutput.setBackground(Color.BLACK);
        consoleOutput.setForeground(Color.GREEN);
        
        JScrollPane scrollPane = new JScrollPane(consoleOutput);
        frame.add(scrollPane, BorderLayout.CENTER);
        
        JTextField commandField = new JTextField();
        commandField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                char keyChar = e.getKeyChar();
                char newLine = '\n';
                if(keyChar == newLine) {
                    String text = commandField.getText();
                    SlimyBot.consoleInput(text);
                    commandField.setText("");
                }
            }
        });
        
        Font commandFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        commandField.setFont(commandFont);
        
        frame.add(commandField, BorderLayout.SOUTH);

        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    
    private static class TextAreaOutputStream extends FilterOutputStream {
        private final JTextArea textArea;
        public TextAreaOutputStream(OutputStream os, JTextArea textArea) {
            super(os);
            Validate.notNull(textArea);
            this.textArea = textArea;
        }
        
        @Override
        public void write(int data) throws IOException {
            super.write(data);
            char ch = (char) data;
            String str = Character.toString(ch);
            this.textArea.append(str);
        }
    }
}
