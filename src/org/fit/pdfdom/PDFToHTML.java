/**
 * PDFToHTML.java
 * (c) Radek Burget, 2011
 *
 * Pdf2Dom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Pdf2Dom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Created on 19.9.2011, 13:34:54 by burgetr
 */
package org.fit.pdfdom;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.fit.pdfdom.resource.HtmlResourceHandler;
import org.fit.pdfdom.resource.IgnoreResourceHandler;
import org.fit.pdfdom.resource.SaveResourceToDirHandler;

/**
 * @author burgetr
 *
 */
public class PDFToHTML
{
    public static void start(String s1, String s2)
    {

        String infile = s1;
        String outfile;
        if (!s2.startsWith("-"))
            outfile = s2;
        else
        {
            String base = s1;
            if (base.toLowerCase().endsWith(".pdf"))
                base = base.substring(0, base.length() - 4);
            outfile = base + ".html";
        }

        PDFDomTreeConfig config = PDFDomTreeConfig.createDefaultConfig();

        HtmlResourceHandler handler = PDFDomTreeConfig.embedAsBase64();
        //HtmlResourceHandler handler = new SaveResourceToDirHandler();
        config.setFontHandler(handler);
        config.setImageHandler(handler);

        PDDocument document = null;
        try
        {
            document = PDDocument.load(new File(infile));
            PDFDomTree parser = new PDFDomTree(config);
            parser.setDisableImageData(false);
            parser.setDisableImages(false);
            parser.setDisableGraphics(false);

            Writer output = new PrintWriter(outfile, "utf-8");
            parser.writeText(document, output);
            output.close();
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            if( document != null )
            {
                try
                {
                    document.close();
                } catch (IOException e) { 
                    System.err.println("Error: " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
    }

}
