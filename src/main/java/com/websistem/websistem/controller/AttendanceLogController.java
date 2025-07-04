package com.websistem.websistem.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Socket;

@Controller
public class AttendanceLogController {

    @GetMapping("/attendance-log")
    public String showForm(@RequestParam(value = "ip", required = false) String ip,
                           @RequestParam(value = "key", required = false) String key,
                           Model model) {
        model.addAttribute("ip", ip != null ? ip : "192.168.1.201");
        model.addAttribute("key", key != null ? key : "0");
        return "attendance-log";
    }

    @PostMapping("/attendance-log/download")
    public void downloadLog(@RequestParam String ip,
                            @RequestParam String key,
                            HttpServletResponse response) throws IOException {
        String soapRequest = "<GetAttLog><ArgComKey xsi:type=\"xsd:integer\">" + key +
                "</ArgComKey><Arg><PIN xsi:type=\"xsd:integer\">All</PIN></Arg></GetAttLog>";
        String newLine = "\r\n";
        StringBuilder buffer = new StringBuilder();

        try (Socket socket = new Socket(ip, 80)) {
            OutputStream out = socket.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write("POST /iWsService HTTP/1.0" + newLine);
            writer.write("Content-Type: text/xml" + newLine);
            writer.write("Content-Length: " + soapRequest.length() + newLine + newLine);
            writer.write(soapRequest + newLine);
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (Exception e) {
            response.setContentType("text/plain");
            // Tambahkan log stacktrace ke file log aplikasi
            e.printStackTrace(); // Ini akan tampil di console/log aplikasi
            response.getWriter().write(
                "Koneksi Gagal: " + e.getClass().getName() + ": " + e.getMessage() +
                "\nCek kemungkinan penyebab:\n" +
                "- Mesin absensi belum support web service/port 80\n" +
                "- Port 80 belum dibuka di mesin absensi\n" +
                "- IP address salah atau mesin tidak online\n" +
                "- Ada firewall yang memblokir port 80\n" +
                "- Coba akses http://" + ip + " dari browser/server\n"
            );
            return;
        }

        // Simple parse: ambil data di antara <GetAttLogResponse>...</GetAttLogResponse>
        String xml = buffer.toString();
        String data = "";
        int start = xml.indexOf("<GetAttLogResponse>");
        int end = xml.indexOf("</GetAttLogResponse>");
        if (start != -1 && end != -1) {
            data = xml.substring(start + 19, end);
        }

        // Set response as downloadable text file
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=attendance-log.txt");

        PrintWriter out = response.getWriter();
        // Header kolom
        out.println("Ac-No\tName\tsTime\tVerify Mode\tMachine\tException");

        // Split per row
        String[] rows = data.split("<Row>");
        for (String row : rows) {
            if (row.contains("</Row>")) {
                String rowData = row.substring(0, row.indexOf("</Row>"));
                String pin = extract(rowData, "<PIN>", "</PIN>");
                String dateTime = extract(rowData, "<DateTime>", "</DateTime>");
                String verified = extract(rowData, "<Verified>", "</Verified>");
                String status = extract(rowData, "<Status>", "</Status>");
                // Mapping Verified ke Verify Mode (contoh sederhana)
                String verifyMode = "PV"; // Default, bisa mapping sesuai kebutuhan
                String name = ""; // Jika ingin ambil nama, perlu query user info
                String machine = "IT OFFICE"; // Default/manual, atau ambil dari mesin jika ada
                String exception = ""; // Default kosong

                out.printf("%s\t%s\t%s\t%s\t%s\t%s%n", pin, name, dateTime, verifyMode, machine, exception);
            }
        }
        out.flush();
        out.close();
    }

    // Helper method to extract value between tags
    private String extract(String src, String startTag, String endTag) {
        int start = src.indexOf(startTag);
        int end = src.indexOf(endTag);
        if (start != -1 && end != -1 && end > start) {
            return src.substring(start + startTag.length(), end).trim();
        }
        return "";
    }

    @PostMapping("/attendance-log/check-connection")
    @ResponseBody
    public String checkConnection(@RequestParam String ip, @RequestParam String key) {
        try (Socket socket = new Socket(ip, 4370)) {
            return "OK";
        } catch (Exception e) {
            return "Gagal: " + e.getMessage();
        }
    }
}