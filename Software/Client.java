package com.example.mgmt382;

import java.net.*;
import java.io.*;
import javax.swing.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
            Thread thread = new Thread(new ClientThread(socket));
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientThread implements Runnable {
    private Socket socket;

    public ClientThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        boolean loginSuccess = false;
        DataOutputStream serverOut = null;
        DataInputStream serverIn = null;
        String email = "";
        boolean isSeller = false;
        try {
            OutputStream outputStream = socket.getOutputStream();
            serverOut = new DataOutputStream(outputStream);
            InputStream inputStream = socket.getInputStream();
            serverIn = new DataInputStream(inputStream);
            int option;
            String password = "";
            loginSuccess = true;
            option = JOptionPane.showConfirmDialog(null, "Welcome to Solar Boost Finance!\nDo you have an account?", "Login", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                boolean isEmail = false;
                boolean endProgram = false;
                do {
                    JTextField textField = new JTextField();
                    Object[] message = {"Enter your email:", textField};
                    option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        email = textField.getText();
                        if (!email.isEmpty() & !email.equals(";")) {
                            serverOut.writeUTF("verifyEmail" + ";" + email);
                            serverOut.flush();
                            isEmail = serverIn.readBoolean();
                        }
                        if (!isEmail) {
                            JOptionPane.showMessageDialog(null, "Email not found. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        endProgram = true;
                        loginSuccess = false;
                        break;
                    }
                } while (!isEmail || !email.contains("@") || email.contains(";"));
                boolean passwordMatches = false;
                if (!endProgram) {
                    do {
                        JTextField textField = new JTextField();
                        Object[] message = {"Enter your password:", textField};
                        option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            password = textField.getText();
                            if (!password.isEmpty()) {
                                serverOut.writeUTF("verifyPassword" + ";" + email + ',' + password);
                                serverOut.flush();
                                passwordMatches = serverIn.readBoolean();
                            }
                            if (!passwordMatches || password.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Password incorrect. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            loginSuccess = false;
                            break;
                        }
                    } while (!passwordMatches || password.isEmpty());
                }
            } else if (option == JOptionPane.NO_OPTION) {
                JOptionPane.showMessageDialog(null, "Please Contact a Solar Boost Representative for Your Login Information.");
                loginSuccess = false;
                /*String newEmail = "";
                boolean isEmail = false;
                do {
                    JTextField textField = new JTextField();
                    Object[] message = {"Enter your new email:", textField};
                    option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        newEmail = textField.getText();
                        if (!newEmail.isEmpty()) {
                            serverOut.writeUTF("verifyEmail" + ";" + newEmail);
                            serverOut.flush();
                            isEmail = serverIn.readBoolean();
                        }
                    } else {
                        loginSuccess = false;
                        break;
                    }
                    if (isEmail) {
                        JOptionPane.showMessageDialog(null, "Email already in use. Please enter a different email.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else if (newEmail.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Error. No email entered. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    if (newEmail.contains(" ") || newEmail.contains(";") || !newEmail.contains("@")) {
                        JOptionPane.showMessageDialog(null, "Error. Email cannot contain spaces or semicolons and must have an @. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } while (isEmail || newEmail.contains(" ") || newEmail.contains(";") || !newEmail.contains("@"));
                email = newEmail;
                String newPassword = "";
                if (loginSuccess) {
                    do {
                        JTextField textField = new JTextField();
                        Object[] message = {"Enter your new password:", textField};
                        option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            newPassword = textField.getText();
                        } else {
                            loginSuccess = false;
                            break;
                        }
                        if (newPassword.contains(";")) {
                            JOptionPane.showMessageDialog(null, "Error. Password cannot contain \";\". Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (newPassword.isEmpty()) {
                            JOptionPane.showMessageDialog(null, "Error. No password entered. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } while (newPassword.contains(";") || newPassword.isEmpty());
                    password = newPassword;
                }
                if (loginSuccess) {
                    String store = "";
                    do {
                        option = JOptionPane.showConfirmDialog(null, "Are you a seller?", "Login", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            boolean isStore = true;
                            do {
                                JTextField textField = new JTextField();
                                Object[] message = {"Enter your new store:", textField};
                                option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);

                                if (option == JOptionPane.OK_OPTION) {
                                    store = textField.getText();
                                    serverOut.writeUTF("checkStore;" + store);
                                    serverOut.flush();
                                    isStore = serverIn.readBoolean();
                                    if (store.isEmpty()) {
                                        JOptionPane.showMessageDialog(null, "Error. No store entered. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                    } else if (isStore) {
                                        JOptionPane.showMessageDialog(null, "Error. Store name already in use. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } while (store.isEmpty() || isStore);
                            serverOut.writeUTF("addSeller" + ";" + email + "," + password + "," + store);
                            serverOut.flush();
                        } else if (option == JOptionPane.NO_OPTION) {
                            serverOut.writeUTF("addCustomer" + ";" + email + "," + password);
                            serverOut.flush();
                        } else {
                            loginSuccess = false;
                            break;
                        }
                    } while (option == JOptionPane.CLOSED_OPTION || option == JOptionPane.CANCEL_OPTION);
                }*/
            } else {
                loginSuccess = false;
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Login Error", "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        if (loginSuccess) {
            JOptionPane.showMessageDialog(null, "Log in successful. Welcome.");
            try {
                serverOut.writeUTF("checkSell1;" + email);
                serverOut.flush();
                isSeller = serverIn.readBoolean();
                serverOut.writeUTF("makeUser;" + email);
                serverOut.flush();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            boolean again;
            do {
                again = false;

                int option;
                Object[] options;
                if (isSeller) {
                    options = new Object[]{"Settings", "Statistics", "Message", "New Store"};
                } else {
                    options = new Object[]{"Settings", "Manage Shares"};
                }
                option = JOptionPane.showOptionDialog(null, "What do you want to do?", "Menu", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (option == 0) { //Settings
                    options = new Object[]{"Change password", "Delete Account"};
                    option = JOptionPane.showOptionDialog(null, "What do you want to do?", "Settings", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                   /* if (option == 0) { //block
                        boolean toIsSeller = false;
                        String emailToBlock = "";
                        boolean isEmail = false;
                        boolean canceled = false;
                        boolean badEmail;
                        do {
                            badEmail = false;
                            JTextField textField = new JTextField();
                            Object[] message = {"Enter the email you wish to block:", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Block", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                emailToBlock = textField.getText();
                            } else {
                                canceled = true;
                                break;
                            }
                            try {
                                if (!emailToBlock.isEmpty()) {
                                    serverOut.writeUTF("checkSell;" + emailToBlock);
                                    serverOut.flush();
                                    toIsSeller = serverIn.readBoolean();
                                    serverOut.writeUTF("verifyEmail;" + emailToBlock);
                                    serverOut.flush();
                                    isEmail = serverIn.readBoolean();
                                }
                                if (!isEmail) {
                                    JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                    badEmail = true;
                                } else if (isSeller) {
                                    if (toIsSeller) {
                                        JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        badEmail = true;

                                    }
                                } else {
                                    if (!toIsSeller) {
                                        JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        badEmail = true;
                                    }
                                }
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        } while (badEmail);
                        if (canceled) {
                            break;
                        }
                        boolean isInvisible;
                        option = JOptionPane.showConfirmDialog(null, "Do you want to become invisible to them?", "Block", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            isInvisible = true;
                        } else if (option == JOptionPane.NO_OPTION) {
                            isInvisible = false;
                        } else {
                            break;
                        }
                        try {
                            serverOut.writeUTF("block;" + isInvisible + "," + emailToBlock);
                            serverOut.flush();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    }*/ if (option == 0) { //change password
                        String newPassword = "";
                        do {
                            JTextField textField = new JTextField();
                            Object[] message = {"Enter your new password:", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Change Password", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                newPassword = textField.getText();
                            } else {
                                break;
                            }
                            if (newPassword.contains(";")) {
                                JOptionPane.showMessageDialog(null, "Error. Password cannot contain \";\". Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (newPassword.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Error. No password entered. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } while (newPassword.contains(";") || newPassword.isEmpty());
                        try {
                            serverOut.writeUTF("changePassword;" + newPassword);
                            serverOut.flush();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    } else if (option == 1) { //delete account
                        option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your account?", "Delete Account", JOptionPane.YES_NO_OPTION);
                        if (option == JOptionPane.YES_OPTION) {
                            try {
                                serverOut.writeUTF("deleteAccount; ");
                                serverOut.flush();
                            } catch (IOException e){
                                JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                            break;
                        }
                    } else if (option == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                } else if (option == 1) { //Manage Shares
                    String[] statLines = null;
                    /*try {
                        serverOut.writeUTF("showStats; " + email);
                        serverOut.flush();
                        int lenList = serverIn.readInt();
                        statLines = new String[lenList];
                        String statLinesS = "";
                        for (int i = 0; i < lenList; i++) {
                            statLines[i] = serverIn.readUTF();
                            statLinesS += statLines[i] + "\n";
                        }*/
                        options = new Object[]{"Buy More Solar Energy", "Reduce Energy Consumption"};
                        option = JOptionPane.showOptionDialog(null, "Total Value of Investment: $ 883\n\n\nBreakdown of Investments by Location\n\nSanta Cruz: $ 210 (+2% since yesterday)\nOakland: $ 390 (+5% since yesterday)\n Irvine: $ 283 (-2% since yesterday) \n\n Total Energy Used (ytd): 101,232 kWh\n\n\n\n\nWhat do you want to do?", "Investments", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        /*if (option == 0) {
                            serverOut.writeUTF("sortStats; " + email);
                            serverOut.flush();
                            serverOut.writeUTF("showStats; " + email);
                            serverOut.flush();
                            lenList = serverIn.readInt();
                            statLines = new String[lenList];
                            statLinesS = "";
                            for (int i = 0; i < lenList; i++) {
                                statLines[i] = serverIn.readUTF();
                                statLinesS += statLines[i] + "\n";
                            }
                            JOptionPane.showMessageDialog(null, statLinesS);
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Error getting statistics.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }*/
                } else if (option == 2) { //Message
                    String emailToMessage = null;
                    String storeToMessage = null;
                    boolean done = false;
                    options = new Object[]{"Browse", "Enter email"};
                    option = JOptionPane.showOptionDialog(null, "Do you want to browse potential message recipients or enter an email to message?", "Create Message", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (option == 0) { //browse
                        String[] browseList = null;
                        String[] blockedBy = null;
                        try {
                            serverOut.writeUTF("browse; ");
                            serverOut.flush();
                            int lenList = serverIn.readInt();
                            browseList = new String[lenList];
                            for (int i = 0; i < lenList; i++) {
                                browseList[i] = serverIn.readUTF();
                            }
                            lenList = serverIn.readInt();
                            blockedBy = new String[lenList];
                            for (int i = 0; i < lenList; i++) {
                                blockedBy[i] = serverIn.readUTF();
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Error getting recipients.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                        boolean blocked;
                        do {
                            blocked = false;
                            if (browseList.length > 0) {
                                option = JOptionPane.showOptionDialog(null, "Choose an option:", "Browse", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, browseList, browseList[0]);
                                if (option != JOptionPane.CLOSED_OPTION) {
                                    if (isSeller) {
                                        emailToMessage = browseList[option];
                                    } else {
                                        storeToMessage = browseList[option];
                                        try {
                                            serverOut.writeUTF("getEmailFromStore;" + storeToMessage);
                                            serverOut.flush();
                                            emailToMessage = serverIn.readUTF();
                                        } catch (IOException e) {
                                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                            e.printStackTrace();
                                        }
                                    }
                                    for (String potEmail : blockedBy) {
                                        if (emailToMessage.equals(potEmail)) {
                                            JOptionPane.showMessageDialog(null, "This user has blocked you. You may not message them.\n Please select a different recipient.");
                                            blocked = true;
                                        }
                                    }
                                } else {
                                    break;
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, "No users to message. Have your customers make an account to message them.");
                                done = true;
                                break;
                            }
                        } while (blocked);
                        if (done) {
                            break;
                        }
                    } else if (option == 1) { //enter email
                        boolean toIsSeller = false;
                        boolean isEmail = false;
                        boolean badEmail;
                        boolean bad = false;
                        do {
                            badEmail = false;
                            JTextField textField = new JTextField();
                            Object[] message = {"Enter the email you wish to message:", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Message", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                emailToMessage = textField.getText();
                            } else {
                                bad = true;
                                break;
                            }
                            String[] browseList;
                            String[] blockedBy;
                            try {
                                serverOut.writeUTF("browse; ");
                                serverOut.flush();
                                int lenList = serverIn.readInt();
                                browseList = new String[lenList];
                                for (int i = 0; i < lenList; i++) {
                                    browseList[i] = serverIn.readUTF();
                                }
                                lenList = serverIn.readInt();
                                blockedBy = new String[lenList];
                                for (int i = 0; i < lenList; i++) {
                                    blockedBy[i] = serverIn.readUTF();
                                }
                                for (String potEmail : browseList) {
                                    if (potEmail.equals(emailToMessage)) {
                                        badEmail = false;
                                        break;
                                    } else {
                                        badEmail = true;
                                    }
                                }
                                if (badEmail) {
                                    JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                for (String blockedByEmail : blockedBy) {
                                    if (blockedByEmail.equals(emailToMessage) & !badEmail) {
                                        badEmail = true;
                                        JOptionPane.showMessageDialog(null, "This user has blocked you. You may not message them.\n Please enter a different recipient.");
                                    }
                                }
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, "Error getting recipients.", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                            try {
                                if (!emailToMessage.isEmpty()) {
                                    serverOut.writeUTF("checkSell;" + emailToMessage);
                                    serverOut.flush();
                                    toIsSeller = serverIn.readBoolean();
                                    serverOut.writeUTF("verifyEmail;" + emailToMessage);
                                    serverOut.flush();
                                    isEmail = serverIn.readBoolean();
                                    if (!isEmail) {
                                        JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        badEmail = true;
                                    }
                                    if (isSeller & toIsSeller) {
                                        JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        badEmail = true;
                                    }
                                    if (!isSeller & !toIsSeller) {
                                        JOptionPane.showMessageDialog(null, "Error. Not a valid email. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                                        badEmail = true;
                                    }
                                }
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        } while (badEmail);
                        if (bad) {
                            break;
                        }
                    } else if (option == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                    if (isSeller) {
                        try {
                            serverOut.writeUTF("getStores; ");
                            serverOut.flush();
                            int numStores = serverIn.readInt();
                            String[] stores = new String[numStores];
                            for (int i = 0; i < numStores; i++) {
                                stores[i] = serverIn.readUTF();
                            }
                            option = JOptionPane.showOptionDialog(null, "Choose one of your stores that is associated with the message:", "Browse", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, stores, stores[0]);
                            if (option != JOptionPane.CLOSED_OPTION) {
                                storeToMessage = stores[option];
                            } else {
                                break;
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    }
                    if (!isSeller & storeToMessage == null) {
                        try {
                            serverOut.writeUTF("getAStore;" + emailToMessage);
                            serverOut.flush();
                            storeToMessage = serverIn.readUTF();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    }
                    try {
                        serverOut.writeUTF("message;" + storeToMessage + "," + emailToMessage);
                        serverOut.flush();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                    String lines = "";
                    try {
                        serverOut.writeUTF("viewMessage; ");
                        serverOut.flush();
                        int lenLines = serverIn.readInt();
                        if (lenLines != -1) {
                            for (int i = 0; i < lenLines; i++) {
                                lines += serverIn.readUTF() + "\n";
                            }
                        }
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                    options = new Object[]{"Send message", "Edit message", "Delete message", "Send text file contents", "Export as csv"};
                    option = JOptionPane.showOptionDialog(null, lines + "\n\nWhat do you want to do?", "Message", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                    if (option == 0) { //send message
                        String messageToSend;
                        boolean badMessage;
                        do {
                            badMessage = false;
                            JTextField textField = new JTextField();
                            Object[] message = {"What do you want to say?", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Create Message", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                messageToSend = textField.getText();
                                if (messageToSend.isEmpty()) {
                                    badMessage = true;
                                    JOptionPane.showMessageDialog(null, "Message cannot be empty. Try again.");
                                }
                                if (!badMessage) {
                                    try {
                                        serverOut.writeUTF("sendMessage;" + messageToSend);
                                        serverOut.flush();
                                        JOptionPane.showMessageDialog(null, "Message sent");
                                    } catch (IOException e) {
                                        JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                break;
                            }
                        } while (badMessage);
                    } else if (option == 1) { //edit message
                        int messageNum = -1;
                        String newMessage;
                        boolean out;
                        boolean badNum;
                        do {
                            out = false;
                            badNum = false;
                            JTextField textField = new JTextField();
                            Object[] message = {lines + "\n\nWhat message number do you want to change?", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Edit Message", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                try {
                                    messageNum = Integer.parseInt(textField.getText());
                                } catch (NumberFormatException e) {
                                    badNum = true;
                                    JOptionPane.showMessageDialog(null, "Error. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                int lastMessageNumber = 0;
                                try {
                                    serverOut.writeUTF("getMessageNum; ");
                                    serverOut.flush();
                                    lastMessageNumber = serverIn.readInt();
                                } catch (IOException e) {
                                    JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                    e.printStackTrace();
                                }
                                if (!badNum) {
                                    if (messageNum > lastMessageNumber || messageNum < 1) {
                                        badNum = true;
                                        JOptionPane.showMessageDialog(null, "Error. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                out = true;
                                break;
                            }
                        } while (badNum);
                        JTextField textField = new JTextField();
                        Object[] message = {lines + "\n\nWhat do you want to change it to?", textField};
                        option = JOptionPane.showConfirmDialog(null, message, "Edit Message", JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            newMessage = textField.getText();
                            try {
                                serverOut.writeUTF("editMessage;" + messageNum + "," + newMessage);
                                serverOut.flush();
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                        if (out) {
                            break;
                        }
                    } else if (option == 2) { //Delete message
                        int messageNum = -1;
                        boolean out;
                        boolean badNum;
                        do {
                            out = false;
                            badNum = false;
                            JTextField textField = new JTextField();
                            Object[] message = {lines + "\n\nWhat message number do you want to delete?", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Delete Message", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                try {
                                    messageNum = Integer.parseInt(textField.getText());
                                } catch (NumberFormatException e) {
                                    badNum = true;
                                    JOptionPane.showMessageDialog(null, "Error. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                                int lastMessageNumber = 0;
                                try {
                                    serverOut.writeUTF("getMessageNum; ");
                                    serverOut.flush();
                                    lastMessageNumber = serverIn.readInt();
                                } catch (IOException e) {
                                    JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                                    e.printStackTrace();
                                }
                                if (!badNum) {
                                    if (messageNum > lastMessageNumber || messageNum < 1) {
                                        badNum = true;
                                        JOptionPane.showMessageDialog(null, "Error. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            } else {
                                out = true;
                                break;
                            }
                        } while (badNum);
                        try {
                            serverOut.writeUTF("deleteMessage;" + messageNum);
                            serverOut.flush();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server error.", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                        if (out) {
                            break;
                        }
                    } else if (option == 3) { //send text file contents
                        boolean badPath;
                        boolean closed;
                        do {
                            badPath = false;
                            closed = false;
                            JTextField textField = new JTextField();
                            Object[] message = {lines + "\n\nEnter the file path of your text file.", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Import text file", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                String filePath = textField.getText();
                                if (!filePath.isEmpty()) {
                                    try {
                                        serverOut.writeUTF("importTxt;" + filePath);
                                        serverOut.flush();
                                        badPath = serverIn.readBoolean();
                                        if (badPath) {
                                            JOptionPane.showMessageDialog(null, "Error. Please enter a valid file path.", "Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    } catch (IOException e) {
                                        JOptionPane.showMessageDialog(null, "Server Error", "Error", JOptionPane.ERROR_MESSAGE);
                                        e.printStackTrace();
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, "Error. Please enter a valid file path.", "Error", JOptionPane.ERROR_MESSAGE);
                                    badPath = true;
                                }
                            } else {
                                closed = true;
                                break;
                            }
                        } while (badPath);
                        if (closed) {
                            break;
                        }
                    } else if (option == 4) { //export as csv
                        boolean badName;
                        boolean closed;
                        String fileName = "";
                        do {
                            closed = false;
                            badName = false;
                            JTextField textField = new JTextField();
                            Object[] message = {lines + "\n\nEnter a file name for your export.", textField};
                            option = JOptionPane.showConfirmDialog(null, message, "Export as CSV", JOptionPane.OK_CANCEL_OPTION);
                            if (option == JOptionPane.OK_OPTION) {
                                fileName = textField.getText();
                                if (fileName.isEmpty()) {
                                    JOptionPane.showMessageDialog(null, "Error. File name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                                    badName = true;
                                }
                            } else {
                                closed = true;
                                break;
                            }
                        } while (badName);
                        try {
                            serverOut.writeUTF("exportAsCSV;" + fileName);
                            serverOut.flush();
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(null, "Server Error", "Error", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                        if (closed) {
                            break;
                        }
                    }else if (option == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                } else if (option == 3) { //new store
                    String newStore = null;
                    boolean isStore = true;
                    do {
                        JTextField textField = new JTextField();
                        Object[] message = {"Enter the name of the store you want to make:", textField};
                        option = JOptionPane.showConfirmDialog(null, message, "New Store", JOptionPane.OK_CANCEL_OPTION);
                        if (option == JOptionPane.OK_OPTION) {
                            newStore = textField.getText();
                            try {
                                if (!newStore.isEmpty()) {
                                    serverOut.writeUTF("checkStore;" + newStore);
                                    serverOut.flush();
                                    isStore = serverIn.readBoolean();
                                }
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(null, "Server Error", "Error", JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                            }
                            if (newStore.isEmpty()) {
                                JOptionPane.showMessageDialog(null, "Error. No store entered. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            } else if (isStore) {
                                JOptionPane.showMessageDialog(null, "Error. Store name already in use. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            break;
                        }
                    } while (isStore || newStore.isEmpty());
                    try {
                        serverOut.writeUTF("newStore;" + newStore);
                        serverOut.flush();
                    } catch (IOException e) {
                        JOptionPane.showMessageDialog(null, "Server Error", "Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                } else if (option == JOptionPane.CLOSED_OPTION) {
                    break;
                }
                option = JOptionPane.showConfirmDialog(null, "Are you finished using the program?", "Finished?", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.NO_OPTION) {
                    again = true;
                }
            } while (again);
            JOptionPane.showMessageDialog(null, "Thanks for using Solar Boost Finance. Goodbye!");
        }
    }
}