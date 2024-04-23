package com.example.mgmt382;

import javax.swing.*;
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle  operations
 *
 * <p>Purdue University -- CS18000 -- Fall 2022 -- Project 4</p>
 *
 * @author group
 * @version April 10, 2022
 */
public class Customer implements Serializable {
    private String email;
    private String password;
    private ArrayList<String> blockedEmails;
    private ArrayList<String> invisibleEmails;
    private File statFile;
    private ArrayList<String> statLines;
    private static String commonWord1;
    private static String commonWord2;
    public Customer(String email) throws IOException {
        this.email = email;
        this.blockedEmails = new ArrayList<>();
        this.invisibleEmails = new ArrayList<>();
        this.statLines = new ArrayList<>();
        statFile = new File("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\" + email + "Stats.txt");
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] elements = line.split(";");
            if (this.email.equals(elements[0])) {
                this.password = elements[1];
                if (elements.length == 3) {
                    String[] blockedEmailsS = elements[2].split(",");
                    for (String blockedEmailS : blockedEmailsS) {
                        if (blockedEmailS.endsWith("T")) {
                            String emailToBlock = blockedEmailS.substring(0, blockedEmailS.length() - 1);
                            invisibleEmails.add(emailToBlock);
                        } else if (blockedEmailS.endsWith("F")) {
                            String emailToBlock = blockedEmailS.substring(0, blockedEmailS.length() - 1);
                            blockedEmails.add(emailToBlock);
                        }
                    }
                }
            }
        }
        br.close();
        if (statFile.createNewFile()) {
            PrintWriter write = new PrintWriter(statFile);
            String[] stores = listStores();
            String line = "Total Amount Invested\n";
            write.write(line);
            write.flush();
            statLines.add(line);
            line = "\n";
            write.write(line);
            write.flush();
            for (String storeS : stores) {
                Store store = new Store(storeS);
                line = storeS + ": " + store.getNumMessagesReceived();
                write.write(line);
                write.flush();
                statLines.add(line);
            }
            line = "\nMessages sent by you\n";
            write.write(line);
            write.flush();
            statLines.add(line);
            line = "\n";
            write.write(line);
            write.flush();
            for (String storeS : stores) {
                Store store = new Store(storeS);
                line = storeS + ": " + store.getNumSentBy(email);
                write.write(line);
                write.flush();
                statLines.add(line);
            }
        } else {
            BufferedReader read = new BufferedReader(new FileReader(statFile));
            String line;
            while ((line = read.readLine()) != null) {
                statLines.add(line);
            }
        }
    }
    public void refreshStats() throws IOException {
        PrintWriter write = new PrintWriter(statFile);
        String[] stores = listStores();
        String line = "Total messages received\n";
        write.write(line);
        write.flush();
        for (String storeS : stores) {
            Store store = new Store(storeS);
            line = "\n" + storeS + ": " + store.getNumMessagesReceived();
            write.write(line);
            write.flush();
        }
        line = "\n\nMessages sent by you\n";
        write.write(line);
        write.flush();
        for (String storeS : stores) {
            Store store = new Store(storeS);
            line = "\n" + storeS + ": " + store.getNumSentBy(email);
            write.write(line);
            write.flush();
        }
        refreshStatField();
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public String[] getBlockedEmails() {
        String[] blockedEmailsArray = new String[blockedEmails.size()];
        for (int i = 0; i < blockedEmails.size(); i++) {
            blockedEmailsArray[i] = blockedEmails.get(i);
        }
        return blockedEmailsArray;
    }

    public String[] getInvisibleEmails() {
        String[] invisibleEmailsArray = new String[invisibleEmails.size()];
        for (int i = 0; i < invisibleEmails.size(); i++) {
            invisibleEmailsArray[i] = invisibleEmails.get(i);
        }
        return invisibleEmailsArray;
    }

    public String[] listStores() throws IOException {
        ArrayList<String> stores = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("sellerLogins.txt"));
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            String[] elements = line.split(";");
            String sellerEmail = elements[0];
            String[] sellerStores = elements[3].split(",");
            if (!invisibleEmails.contains(sellerEmail)) {
                stores.addAll(Arrays.asList(sellerStores));
            }
        }
        return stores.toArray(new String[0]);
    }
    public void block(Seller sellerToBlock, boolean isInvisible) throws IOException {
        String sellerEmail = sellerToBlock.getEmail();
        String flag = null;
        if (isInvisible) {
            if (!invisibleEmails.contains(sellerEmail)) {
                invisibleEmails.add(sellerEmail);
                flag = "T";
            }
        } else {
            if (!blockedEmails.contains(sellerEmail)) {
                blockedEmails.add(sellerEmail);
                flag = "F";
            }
        }
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("sellerLogins.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(sellerEmail) & !line.contains(this.email)) {
                String[] elements = line.split(";");
                if (!elements[2].isEmpty()) {
                    line = elements[0] + ";" + elements[1] + ";" + elements[2] + "," + this.email + flag + ";" + elements[3];
                } else {
                    line = elements[0] + ";" + elements[1] + ";" + this.email + flag + ";" + elements[3];
                }
            }
            lines.add(line);
        }
        br.close();
        PrintWriter pw = new PrintWriter("sellerLogins.txt");
        boolean firstLineDone = false;
        for (String line2 : lines) {
            if (firstLineDone) {
                pw.write("\n" + line2);
                pw.flush();
            } else {
                pw.write(line2);
                pw.flush();
                firstLineDone = true;
            }
        }
        pw.close();
    }
    public void changePassword(String newPassword) throws IOException {
        this.password = newPassword;
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"));
        ArrayList<String> lines = new ArrayList<>();
        String line = br.readLine();
        while (line != null) {
            if (line.contains(this.email)) {
                String[] elements = line.split(";");
                elements[1] = newPassword;
                line = String.join(";", elements);
            }
            lines.add(line);
            line = br.readLine();
        }
        br.close();
        PrintWriter pw = new PrintWriter("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt");
        boolean firstLineDone = false;
        for (String content : lines) {
            if (firstLineDone) {
                pw.write("\n" + content);
                pw.flush();
            } else {
                pw.write(content);
                pw.flush();
                firstLineDone = true;
            }
        }
        pw.close();
    }
    public void deleteAccount() throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt"));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains(this.email)) {
                continue;
            }
            lines.add(line);
        }
        br.close();
        PrintWriter pw = new PrintWriter("C:\\Users\\cardw\\Documents\\CS 180\\MGMT382\\src\\main\\java\\com\\example\\mgmt382\\customerLogins.txt");
        boolean firstLineDone = false;
        for (String line2 : lines) {
            if (firstLineDone) {
                pw.write("\n" + line2);
                pw.flush();
            } else {
                pw.write(line2);
                pw.flush();
                firstLineDone = true;
            }
        }
        pw.close();
    }
    public String[] getStats() throws IOException {
        String[] stats = statLines.toArray(new String[0]);
        return stats;
    }
    public void refreshStatField() throws IOException {
        statLines = new ArrayList<>();
        BufferedReader read = new BufferedReader(new FileReader(statFile));
        String line;
        while ((line = read.readLine()) != null) {
            statLines.add(line);
        }
    }

    public void sortStats() throws IOException {
        System.out.println("sorting");
        BufferedReader br = new BufferedReader(new FileReader(statFile));
        String line;
        ArrayList<String> lines = new ArrayList<>();
        ArrayList<String> topStores = new ArrayList<>();
        ArrayList<String> bottomStores = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            if (line.contains("Total messages received")) {
                lines.add(line);
                lines.add(br.readLine());
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    topStores.add(line);
                }
                for (String str : topStores) {
                    System.out.println(str);
                }
                for (int i = 0; i < topStores.size() - 1; i++) {
                    for (int j = i + 1; j < topStores.size(); j++) {
                        String s1 = topStores.get(i);
                        String s2 = topStores.get(j);
                        int n1 = Integer.parseInt(s1.substring(s1.lastIndexOf(":") + 1).trim());
                        int n2 = Integer.parseInt(s2.substring(s2.lastIndexOf(":") + 1).trim());
                        if (n1 < n2) {
                            topStores.set(i, s2);
                            topStores.set(j, s1);
                        }
                    }
                }
                for (String str : topStores) {
                    System.out.println(str);
                }
                for (String store : topStores) {
                    lines.add(store);
                }
            } else if (line.contains("Messages sent by you")) {
                lines.add(line);
                lines.add(br.readLine());
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    bottomStores.add(line);
                }
                for (int i = 0; i < bottomStores.size() - 1; i++) {
                    for (int j = i + 1; j < bottomStores.size(); j++) {
                        String s1 = bottomStores.get(i);
                        String s2 = bottomStores.get(j);
                        int n1 = Integer.parseInt(s1.substring(s1.lastIndexOf(":") + 1).trim());
                        int n2 = Integer.parseInt(s2.substring(s2.lastIndexOf(":") + 1).trim());
                        if (n1 < n2) {
                            bottomStores.set(i, s2);
                            bottomStores.set(j, s1);
                        }
                    }
                }
                for (String store : bottomStores) {
                    if (store != null) {
                        lines.add(store);
                    }
                }
            }
            if (line != null) {
                lines.add(line);
            }
        }
        br.close();
        PrintWriter write = new PrintWriter(statFile);
        boolean firstLineDone = false;
        for (String line2 : lines) {
            if (firstLineDone) {
                write.write("\n" + line2);
                write.flush();
            } else {
                write.write(line2);
                write.flush();
                firstLineDone = true;
            }
        }
        refreshStatField();
    }
}
