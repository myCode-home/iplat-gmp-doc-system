package org.dromara.dms.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 极简 PDF 生成器（仅 ASCII 内容，用于本地演示环境的占位预览文件）
 */
public class SimplePdfWriter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成一页 PDF
     *
     * @param title 标题（非 ASCII 字符会被替换）
     * @param lines 正文行
     */
    public static byte[] write(String title, List<String> lines) {
        StringBuilder content = new StringBuilder();
        int y = 760;
        content.append("BT /F1 18 Tf 60 ").append(y).append(" Td (").append(ascii(title)).append(") Tj ET\n");
        y -= 40;
        content.append("BT /F1 11 Tf 60 ").append(y).append(" Td (Generated at: ")
            .append(LocalDateTime.now().format(FORMATTER)).append(") Tj ET\n");
        y -= 30;
        for (String line : lines) {
            content.append("BT /F1 11 Tf 60 ").append(y).append(" Td (").append(ascii(line)).append(") Tj ET\n");
            y -= 20;
            if (y < 60) {
                break;
            }
        }
        String stream = content.toString();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.writeBytes("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
            out.writeBytes("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            out.writeBytes("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            out.writeBytes("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n"
                .getBytes(StandardCharsets.ISO_8859_1));
            byte[] streamBytes = stream.getBytes(StandardCharsets.ISO_8859_1);
            out.writeBytes(("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
            out.writeBytes(streamBytes);
            out.writeBytes("\nendstream\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            out.writeBytes("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
            String xrefPos = Integer.toString(out.size());
            out.writeBytes("xref\n0 6\n0000000000 65535 f \n".getBytes(StandardCharsets.ISO_8859_1));
            // 逐个对象写出偏移量（粗略方式：重建一次以获取真实偏移）
            out.reset();
            StringBuilder xref = new StringBuilder();
            int[] offsets = new int[6];
            StringBuilder body = new StringBuilder();
            body.append("%PDF-1.4\n");
            offsets[1] = body.length();
            body.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            offsets[2] = body.length();
            body.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
            offsets[3] = body.length();
            body.append("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
            offsets[4] = body.length();
            body.append("4 0 obj\n<< /Length ").append(streamBytes.length).append(" >>\nstream\n").append(stream).append("\nendstream\nendobj\n");
            offsets[5] = body.length();
            body.append("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
            body.append("xref\n0 6\n");
            xref.append(String.format("%010d 65535 f \n", 0));
            for (int i = 1; i <= 5; i++) {
                xref.append(String.format("%010d 00000 n \n", offsets[i]));
            }
            body.append(xref);
            body.append("trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n").append(body.length() - xref.length()).append("\n%%EOF");
            out.writeBytes(body.toString().getBytes(StandardCharsets.ISO_8859_1));
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("生成 PDF 失败", e);
        }
    }

    private static String ascii(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            sb.append(c > 126 || c < 32 ? '?' : (c == '(' || c == ')' || c == '\\' ? ' ' : c));
        }
        return sb.toString();
    }

    private SimplePdfWriter() {
    }
}
