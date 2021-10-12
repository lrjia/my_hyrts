package set.hyrts.org.objectweb.asm.xml;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;
import set.hyrts.org.objectweb.asm.ClassReader;

public class Processor {
   public static final int BYTECODE = 1;
   public static final int MULTI_XML = 2;
   public static final int SINGLE_XML = 3;
   private static final String SINGLE_XML_NAME = "classes.xml";
   private final int inRepresentation;
   private final int outRepresentation;
   private final InputStream input;
   private final OutputStream output;
   private final Source xslt;
   private int n = 0;

   public Processor(int var1, int var2, InputStream var3, OutputStream var4, Source var5) {
      this.inRepresentation = var1;
      this.outRepresentation = var2;
      this.input = var3;
      this.output = var4;
      this.xslt = var5;
   }

   public int process() throws TransformerException, IOException, SAXException {
      ZipInputStream var1 = new ZipInputStream(this.input);
      ZipOutputStream var2 = new ZipOutputStream(this.output);
      OutputStreamWriter var3 = new OutputStreamWriter(var2);
      Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
      TransformerFactory var4 = TransformerFactory.newInstance();
      if (var4.getFeature("http://javax.xml.transform.sax.SAXSource/feature") && var4.getFeature("http://javax.xml.transform.sax.SAXResult/feature")) {
         SAXTransformerFactory var5 = (SAXTransformerFactory)var4;
         Templates var6 = null;
         if (this.xslt != null) {
            var6 = var5.newTemplates(this.xslt);
         }

         Processor$EntryElement var7 = this.getEntryElement(var2);
         Object var8 = null;
         switch(this.outRepresentation) {
         case 1:
            var8 = new Processor$OutputSlicingHandler(new Processor$ASMContentHandlerFactory(var2), var7, false);
            break;
         case 2:
            var8 = new Processor$OutputSlicingHandler(new Processor$SAXWriterFactory(var3, true), var7, true);
            break;
         case 3:
            ZipEntry var9 = new ZipEntry("classes.xml");
            var2.putNextEntry(var9);
            var8 = new Processor$SAXWriter(var3, false);
         }

         Object var14;
         if (var6 == null) {
            var14 = var8;
         } else {
            var14 = new Processor$InputSlicingHandler("class", (ContentHandler)var8, new Processor$TransformerHandlerFactory(var5, var6, (ContentHandler)var8));
         }

         Processor$SubdocumentHandlerFactory var10 = new Processor$SubdocumentHandlerFactory((ContentHandler)var14);
         if (var14 != null && this.inRepresentation != 3) {
            ((ContentHandler)var14).startDocument();
            ((ContentHandler)var14).startElement("", "classes", "classes", new AttributesImpl());
         }

         int var11;
         ZipEntry var12;
         for(var11 = 0; (var12 = var1.getNextEntry()) != null; ++var11) {
            this.update(var12.getName(), this.n++);
            if (this.isClassEntry(var12)) {
               this.processEntry(var1, var12, var10);
            } else {
               OutputStream var13 = var7.openEntry(this.getName(var12));
               this.copyEntry(var1, var13);
               var7.closeEntry();
            }
         }

         if (var14 != null && this.inRepresentation != 3) {
            ((ContentHandler)var14).endElement("", "classes", "classes");
            ((ContentHandler)var14).endDocument();
         }

         if (this.outRepresentation == 3) {
            var2.closeEntry();
         }

         var2.flush();
         var2.close();
         return var11;
      } else {
         return 0;
      }
   }

   private void copyEntry(InputStream var1, OutputStream var2) throws IOException {
      if (this.outRepresentation != 3) {
         byte[] var3 = new byte[2048];

         int var4;
         while((var4 = var1.read(var3)) != -1) {
            var2.write(var3, 0, var4);
         }

      }
   }

   private boolean isClassEntry(ZipEntry var1) {
      String var2 = var1.getName();
      return this.inRepresentation == 3 && var2.equals("classes.xml") || var2.endsWith(".class") || var2.endsWith(".class.xml");
   }

   private void processEntry(ZipInputStream var1, ZipEntry var2, Processor$ContentHandlerFactory var3) {
      ContentHandler var4 = var3.createContentHandler();

      try {
         boolean var5 = this.inRepresentation == 3;
         if (this.inRepresentation == 1) {
            ClassReader var6 = new ClassReader(readEntry(var1, var2));
            var6.accept(new SAXClassAdapter(var4, var5), 0);
         } else {
            XMLReader var8 = XMLReaderFactory.createXMLReader();
            var8.setContentHandler(var4);
            var8.parse(new InputSource((InputStream)(var5 ? new Processor$ProtectedInputStream(var1) : new ByteArrayInputStream(readEntry(var1, var2)))));
         }
      } catch (Exception var7) {
         this.update(var2.getName(), 0);
         this.update(var7, 0);
      }

   }

   private Processor$EntryElement getEntryElement(ZipOutputStream var1) {
      return (Processor$EntryElement)(this.outRepresentation == 3 ? new Processor$SingleDocElement(var1) : new Processor$ZipEntryElement(var1));
   }

   private String getName(ZipEntry var1) {
      String var2 = var1.getName();
      if (this.isClassEntry(var1)) {
         if (this.inRepresentation != 1 && this.outRepresentation == 1) {
            var2 = var2.substring(0, var2.length() - 4);
         } else if (this.inRepresentation == 1 && this.outRepresentation != 1) {
            var2 = var2 + ".xml";
         }
      }

      return var2;
   }

   private static byte[] readEntry(InputStream var0, ZipEntry var1) throws IOException {
      long var2 = var1.getSize();
      int var6;
      if (var2 > -1L) {
         byte[] var7 = new byte[(int)var2];

         for(int var8 = 0; (var6 = var0.read(var7, var8, var7.length - var8)) > 0; var8 += var6) {
         }

         return var7;
      } else {
         ByteArrayOutputStream var4 = new ByteArrayOutputStream();
         byte[] var5 = new byte[4096];

         while((var6 = var0.read(var5)) != -1) {
            var4.write(var5, 0, var6);
         }

         return var4.toByteArray();
      }
   }

   protected void update(Object var1, int var2) {
      if (var1 instanceof Throwable) {
         ((Throwable)var1).printStackTrace();
      } else if (var2 % 100 == 0) {
         System.err.println(var2 + " " + var1);
      }

   }

   public static void main(String[] var0) throws Exception {
      if (var0.length < 2) {
         showUsage();
      } else {
         int var1 = getRepresentation(var0[0]);
         int var2 = getRepresentation(var0[1]);
         Object var3 = System.in;
         BufferedOutputStream var4 = new BufferedOutputStream(System.out);
         StreamSource var5 = null;

         for(int var6 = 2; var6 < var0.length; ++var6) {
            if ("-in".equals(var0[var6])) {
               ++var6;
               var3 = new FileInputStream(var0[var6]);
            } else if ("-out".equals(var0[var6])) {
               ++var6;
               var4 = new BufferedOutputStream(new FileOutputStream(var0[var6]));
            } else {
               if (!"-xslt".equals(var0[var6])) {
                  showUsage();
                  return;
               }

               ++var6;
               var5 = new StreamSource(new FileInputStream(var0[var6]));
            }
         }

         if (var1 != 0 && var2 != 0) {
            Processor var12 = new Processor(var1, var2, (InputStream)var3, var4, var5);
            long var7 = System.currentTimeMillis();
            int var9 = var12.process();
            long var10 = System.currentTimeMillis();
            System.err.println(var9);
            System.err.println(var10 - var7 + "ms  " + 1000.0F * (float)var9 / (float)(var10 - var7) + " resources/sec");
         } else {
            showUsage();
         }
      }
   }

   private static int getRepresentation(String var0) {
      if ("code".equals(var0)) {
         return 1;
      } else if ("xml".equals(var0)) {
         return 2;
      } else {
         return "singlexml".equals(var0) ? 3 : 0;
      }
   }

   private static void showUsage() {
      System.err.println("Usage: Main <in format> <out format> [-in <input jar>] [-out <output jar>] [-xslt <xslt fiel>]");
      System.err.println("  when -in or -out is omitted sysin and sysout would be used");
      System.err.println("  <in format> and <out format> - code | xml | singlexml");
   }
}
