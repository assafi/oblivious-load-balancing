/**
 * Oblivious Load Balance Simulator
 *
 * 236635 - On the Management and Efficiency of Cloud Based Services (W 2011)
 * CS Faculty, Technion - Institute of Technology 
 *
 * Authors: Assaf Israel, Eli Nazarov, Asi Bross 
 * 
 */
package misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * @author Assaf
 *  
 */
public class CsvWriter implements IDataFileWriter {

	/**
	 * 
	 */
	private static final String DELIMITER = ",";
	private String newline = System.getProperty("line.separator");
	private OutputStreamWriter writer = null;
	private String[] labels = null;
	private int nbrCols = 0;
	private int nbrRows = 0;
	private int index = 0;

	/**
	 * 
	 */
	public CsvWriter() {
	}

	/**
	 * @param file
	 *            The file to write to
	 * @param encoding
	 *            encoding method
	 * @throws IOException
	 *             In case file is invalid, or can't open stream
	 */
	public CsvWriter(File file, String encoding) throws IOException {
		openFile(file, encoding);
	}

	public void openFile(File file, String encoding) throws IOException {
		if (null == encoding) {
			encoding = System.getProperty("file.encoding");
		}

		FileOutputStream fout = new FileOutputStream(file,true);

		this.writer = new OutputStreamWriter(fout, encoding);
	}

	public void writeLabels(String[] labels) throws IOException {
		this.nbrCols = labels.length;
		writeData(labels);
		this.labels = labels.clone();
	}

	public void writeData(Map<String, String> data) throws IOException {

		if (null == data) {
			return;
		}

		String[] row = new String[nbrCols];
		for (int i = 0; i < labels.length; i++) {
			if (data.containsKey(labels[i])) {
				row[i] = data.get(labels[i]);
				/*
				 * Binary transformation
				 */
				if (row[i] != null && row[i].equals("true")){
					row[i] = "1";
				}
				if (row[i] != null && row[i].equals("false")){
					row[i] = "0";
				}
			} else {
				row[i] = "0";
			}
		}
		/*
		 * Index
		 */
		row[0] = Integer.toString(index++);

		writeData(row);
	}

	public void close() throws IOException {
		if (null != this.writer){
			this.writer.close();
		}
	}

	private void writeData(String[] values) throws IOException {

		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				this.writer.write(DELIMITER);
			}

			if (values[i] != null) {
				this.writer.write("\"");
				this.writer.write(this.csvValue(values[i]));
				this.writer.write("\"");
			}
		}

		this.writer.write(this.newline);
		this.nbrRows++;
	}

	private String csvValue(String str) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			sb.append(c);

			switch (c) {

			case '"':
				sb.append('"');
				break;
			}
		}

		return sb.toString();
	}

	public String getExtension() {
		return new String("csv");
	}
}
