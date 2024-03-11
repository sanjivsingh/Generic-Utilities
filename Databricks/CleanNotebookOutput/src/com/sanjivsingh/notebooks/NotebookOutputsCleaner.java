package com.sanjivsingh.notebooks;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * The Class NotebookOutputsCleaner.
 */
public class NotebookOutputsCleaner {

	/** The Constant NOTEBOOK_CELL_OUTPUTS. */
	private static final String NOTEBOOK_CELL_OUTPUTS = "outputs";

	/** The Constant NOTEBOOK_CELLS. */
	private static final String NOTEBOOK_CELLS = "cells";

	/** The Constant NOTEBOOK_EXTN. */
	private static final String NOTEBOOK_EXTN = ".ipynb";

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		String inputDirectory = validateAndGetPath(args);
		processFile(new File(inputDirectory));
	}

	/**
	 * Validate And Get Path.
	 *
	 * @param args the args
	 * @return the string
	 */
	private static String validateAndGetPath(String[] args) {
		if (args.length == 0) {
			throw new RuntimeException("Invalid Argument : Input Path not provided");
		}

		String inputDirectory = args[0];
		if (!new File(inputDirectory).isDirectory()) {
			throw new RuntimeException("Path must be a directory : " + inputDirectory);
		}
		return inputDirectory;
	}

	/**
	 * Process file.
	 *
	 * @param file the file
	 */
	private static void processFile(File file) {
		if (file.isHidden()) {
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			for (File childFile : childFiles) {
				processFile(childFile);
			}
		}
		if (isNotebookFile(file)) {
			cleanNotebook(file);
		}
	}

	/**
	 * Checks if is notebook file.
	 *
	 * @param file the file
	 * @return true, if is notebook file
	 */
	private static boolean isNotebookFile(File file) {
		return file.isFile() && file.getName().endsWith(NOTEBOOK_EXTN);
	}

	/** The file updated. */
	private static boolean fileUpdated;

	/**
	 * Clean notebook.
	 *
	 * @param file the file
	 */
	private static void cleanNotebook(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		String filePath = file.getAbsolutePath();
		fileUpdated = false;
		JSONObject fileObj = null;
		JSONParser jsonParser = new JSONParser();
		try (FileReader reader = new FileReader(filePath)) {
			// Read JSON file
			fileObj = (JSONObject) jsonParser.parse(reader);
			((JSONArray) fileObj.get(NOTEBOOK_CELLS)).forEach(cell -> parseCellObject((JSONObject) cell));

		} catch (Exception e) {
			System.err.println("Error while processing notebook : " + filePath + " Error : " + e.getMessage());
		}

		if (fileUpdated) {
			writeNotebookFile(filePath, fileObj);
		}
	}

	/**
	 * Write notebook file.
	 *
	 * @param filePath the file path
	 * @param fileObj  the file obj
	 */
	private static void writeNotebookFile(String filePath, JSONObject fileObj) {
		System.out.println("---------------Rewritting Notebook : " + filePath);
		try (FileWriter writefile = new FileWriter(filePath)) {
			writefile.write(fileObj.toJSONString());
			writefile.flush();
		} catch (IOException e) {
			System.err.println("Error while Rewritting notebook : " + filePath + " Error : " + e.getMessage());
		}
	}

	/**
	 * Parses the cell object.
	 *
	 * @param cell the cell
	 */
	private static void parseCellObject(JSONObject cell) {
		Object outputsOjb = cell.get(NOTEBOOK_CELL_OUTPUTS);
		if (haveCellOutputs(outputsOjb)) {
			((JSONArray) outputsOjb).clear();
			fileUpdated = true;

		}
	}

	/**
	 * Have cell outputs.
	 *
	 * @param outputsOjb the outputs ojb
	 * @return true, if successful
	 */
	private static boolean haveCellOutputs(Object outputsOjb) {
		return outputsOjb != null && !((JSONArray) outputsOjb).isEmpty();
	}
}
