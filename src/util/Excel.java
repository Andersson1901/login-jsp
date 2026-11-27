// Autor: Rodriguez Alvarado Andersson Eduardo
package util;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// Genera un libro .xlsx (un zip de XML) sin librerías externas. La primera fila de cada hoja es el encabezado.
// Los textos se guardan como texto, nunca como fórmulas; BigDecimal se muestra con 2 decimales.
public final class Excel {
    private static final String ESPACIO="http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final String RELACIONES="http://schemas.openxmlformats.org/package/2006/relationships";
    private static final String DOCUMENTO="http://schemas.openxmlformats.org/officeDocument/2006/relationships";
    private static final String CABECERA="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private Excel() { }

    public static void escribir(OutputStream salida,Map<String,List<Object[]>> hojas) throws IOException {
        List<String> nombres=new ArrayList<>(hojas.keySet());
        try(ZipOutputStream zip=new ZipOutputStream(salida,StandardCharsets.UTF_8)) {
            StringBuilder tipos=new StringBuilder(CABECERA+"<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/><Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>");
            StringBuilder libro=new StringBuilder(CABECERA+"<workbook xmlns=\""+ESPACIO+"\" xmlns:r=\""+DOCUMENTO+"\"><sheets>");
            StringBuilder enlaces=new StringBuilder(CABECERA+"<Relationships xmlns=\""+RELACIONES+"\">");
            for(int i=1;i<=nombres.size();i++) {
                tipos.append("<Override PartName=\"/xl/worksheets/sheet"+i+".xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>");
                libro.append("<sheet name=\""+xml(nombreHoja(nombres.get(i-1)))+"\" sheetId=\""+i+"\" r:id=\"rId"+i+"\"/>");
                enlaces.append("<Relationship Id=\"rId"+i+"\" Type=\""+DOCUMENTO+"/worksheet\" Target=\"worksheets/sheet"+i+".xml\"/>");
            }
            enlaces.append("<Relationship Id=\"rId"+(nombres.size()+1)+"\" Type=\""+DOCUMENTO+"/styles\" Target=\"styles.xml\"/></Relationships>");
            parte(zip,"[Content_Types].xml",tipos.append("</Types>").toString());
            parte(zip,"_rels/.rels",CABECERA+"<Relationships xmlns=\""+RELACIONES+"\"><Relationship Id=\"rId1\" Type=\""+DOCUMENTO+"/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            parte(zip,"xl/workbook.xml",libro.append("</sheets></workbook>").toString());
            parte(zip,"xl/_rels/workbook.xml.rels",enlaces.toString());
            parte(zip,"xl/styles.xml",CABECERA+"<styleSheet xmlns=\""+ESPACIO+"\"><fonts count=\"2\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font><font><b/><sz val=\"11\"/><name val=\"Calibri\"/></font></fonts><fills count=\"2\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill></fills><borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders><cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs><cellXfs count=\"3\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/><xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/><xf numFmtId=\"2\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/></cellXfs><cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>");
            for(int i=1;i<=nombres.size();i++) parte(zip,"xl/worksheets/sheet"+i+".xml",hoja(hojas.get(nombres.get(i-1))));
        }
    }

    private static String hoja(List<Object[]> filas) {
        int columnas=filas.stream().mapToInt(f -> f.length).max().orElse(1);
        StringBuilder x=new StringBuilder(CABECERA+"<worksheet xmlns=\""+ESPACIO+"\"><cols><col min=\"1\" max=\""+columnas+"\" width=\"24\" customWidth=\"1\"/></cols><sheetData>");
        for(int f=0;f<filas.size();f++) {
            x.append("<row r=\""+(f+1)+"\">");
            Object[] fila=filas.get(f);
            for(int c=0;c<fila.length;c++) {
                Object valor=fila[c]; if(valor==null) continue;
                String ref=columna(c)+(f+1);
                if(valor instanceof BigDecimal) x.append("<c r=\""+ref+"\" s=\"2\"><v>"+((BigDecimal)valor).toPlainString()+"</v></c>");
                else if(valor instanceof Number) x.append("<c r=\""+ref+"\"><v>"+valor+"</v></c>");
                else x.append("<c r=\""+ref+"\" t=\"inlineStr\""+(f==0?" s=\"1\"":"")+"><is><t xml:space=\"preserve\">"+xml(texto(valor.toString(),32767))+"</t></is></c>");
            }
            x.append("</row>");
        }
        return x.append("</sheetData></worksheet>").toString();
    }

    private static String columna(int indice) {
        StringBuilder letras=new StringBuilder();
        for(int i=indice;i>=0;i=i/26-1) letras.insert(0,(char)('A'+i%26));
        return letras.toString();
    }
    // Excel limita el nombre de la hoja a 31 caracteres y no admite estos símbolos.
    private static String nombreHoja(String nombre) {
        String limpio=nombre.replaceAll("[\\[\\]:*?/\\\\]"," ").trim();
        return limpio.isEmpty() ? "Hoja" : limpio.substring(0,Math.min(31,limpio.length()));
    }
    private static String texto(String valor,int maximo) { return valor.length()>maximo ? valor.substring(0,maximo) : valor; }
    // Quita los caracteres que XML 1.0 no admite y escapa los que tienen significado.
    private static String xml(String valor) {
        StringBuilder x=new StringBuilder();
        valor.codePoints().filter(p -> p==9 || p==10 || p==13 || (p>=0x20 && p<=0xD7FF) || (p>=0xE000 && p<=0xFFFD) || (p>=0x10000 && p<=0x10FFFF)).forEach(p -> {
            if(p=='&') x.append("&amp;"); else if(p=='<') x.append("&lt;"); else if(p=='>') x.append("&gt;"); else if(p=='"') x.append("&quot;"); else x.appendCodePoint(p);
        });
        return x.toString();
    }
    private static void parte(ZipOutputStream zip,String nombre,String contenido) throws IOException {
        zip.putNextEntry(new ZipEntry(nombre)); zip.write(contenido.getBytes(StandardCharsets.UTF_8)); zip.closeEntry();
    }
}
