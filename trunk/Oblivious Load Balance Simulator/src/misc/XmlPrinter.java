/**
 * 
 */
package misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX printer handler for the PetriNet xml files.
 * @author Eli Nazarov
 *
 */
public class XmlPrinter implements ContentHandler {
	private Writer out;
	private int depth = 0;  // depth in hierarchy

	public XmlPrinter(File out) {
		try {
			this.out = new OutputStreamWriter(new FileOutputStream(out), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Bad OutputStream");
		} catch (FileNotFoundException e) {
			System.out.println("Bad OutputStream");
		}
	}

	public void setDocumentLocator(Locator locator) {
	}

	public void startDocument() throws SAXException {

		depth = 0; // so instance can be reused
		try {
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}

	}

	public void endDocument() throws SAXException {
		try {
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void startElement(String uri, String localName, String qName,Attributes atts)
			throws SAXException {
		
		String element = new String();
		element += "<" + qName + " ";
		for (int i = 0; i < atts.getLength(); i++) {
			element += atts.getQName(i) + "=\"" + atts.getValue(i) +"\" ";
		}
		element +=  ">\r\n";
		try {
			indent();
			out.write(element);
			depth++;
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void singleElement(String uri, String localName, String qName,Attributes atts)
	throws SAXException {

		String element = new String();
		element += "<" + qName + " ";
		for (int i = 0; i < atts.getLength(); i++) {
			element += atts.getQName(i) + "=\"" + atts.getValue(i) +"\" ";
		}
		element +=  "/>\r\n";
		try {
			indent();
			out.write(element);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try {
			depth--;
			indent();
			out.write("</" + qName + ">\r\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void characters(char[] text, int start, int length)
			throws SAXException {
		try {
			indent();
			out.write(text, start, length);
			out.write("\r\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	public void ignorableWhitespace(char[] text, int start, int length)
			throws SAXException {

	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		try {
			indent();
			out.write("<?" + target + " " + data + "?>\r\n");
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void indent() throws IOException {

		int spaces = 2; // number of spaces to indent

		for (int i = 0; i < depth * spaces; i++) {
			out.write(' ');
		}
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {

	}

}
