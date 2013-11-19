package com.furry.robot.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.junit.Assert;
import org.junit.ComparisonFailure;

/**
 * helps to test if the test data is stored as files. This fixture can read those test files and
 * pass the data to the program and validate the output from the program with the output file
 */
public abstract class ConsoleValidatingTestFixture {
    private Class<?> clazz;
    private String[] methodsToExecute = { "run", "main" };

    /**
     * Name of the input file with extension. Example 110101.inp. Override if any other custom
     * location
     * 
     * @return
     */
    protected File getInputFile(final String fileName) {
	return getFile(new File(System.getProperty("user.dir")), fileName);
    }

    /**
     * Name of the output file. Example : 110101.oup. Override if any other custom location
     * 
     * @param fileName
     * @return
     */
    protected File getOutputFile(final String fileName) {
	return getFile(new File(System.getProperty("user.dir")), fileName);
    }

    /**
     * Most of the judges use main or run method, if you are using any other method, overide this to
     * specify custom method.
     * 
     * @return
     */
    protected String[] getMethodsToExecute() {
	return methodsToExecute;
    }

    /**
     * Provide the class being validated
     * 
     * @param clazz
     */
    public ConsoleValidatingTestFixture(Class<?> clazz) {
	this.clazz = clazz;
    }

    private Class<?> getClazz() {
	return this.clazz;
    }

    /**
     * Calling this method, would do the following <br/>
     * <ol>
     * <li>create an instance of the class being tested in other System Under Test (SUT).</li>
     * <li>Open the test input file and pass this input to the instance of the SUT being tested</li>
     * <li>Collect the test data produced by the SUT class</li>
     * <li>Validate the output produced with that of the test output file</li>
     * </ol>
     * 
     * @param inputFile
     *            file to get the test data from
     * @param outputFile
     *            file to validate the test output
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    public void sendTestFileContentAndValidatedAgainstFileContent(String inputFile, String outputFile) throws IOException, InstantiationException,
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException {

	PrintStream consoleOutstream = System.out;
	InputStream consoleInStream = System.in;
	String programResult = null;

	try {
	    ByteArrayOutputStream testOutStream = interceptTheProgramOutput();

	    readInputFileContent(inputFile);

	    executeEntryMethod();

	    programResult = testOutStream.toString();
	    validateProgramOutputAgainstOutputfileContent(outputFile, programResult);

	}
	catch (ComparisonFailure exception) {
	    outputResultToConsole(outputFile, consoleOutstream, programResult);
	    throw exception;
	}
	finally {
	    System.setIn(consoleInStream);
	    System.setOut(consoleOutstream);
	}

    }

    private void outputResultToConsole(String outputFile, PrintStream consoleOutstream, String programResult) {
	consoleOutstream.append("Test Result compared for the output file:" + outputFile + "\n");
	consoleOutstream.append(programResult);
	consoleOutstream.append("\n");
    }

    private void validateProgramOutputAgainstOutputfileContent(String outputFile, String result) throws FileNotFoundException, IOException {
	File outfile = getOutputFile(outputFile);
	BufferedReader br = new BufferedReader(new FileReader(outfile));
	String sCurrentLine;
	Scanner scanner = new Scanner(result);
	int i = 0;
	while ((sCurrentLine = br.readLine()) != null) {
	    String nextLine = scanner.nextLine();
	    Assert.assertEquals("Error in line :" + ++i + " of file:" + outputFile, sCurrentLine, nextLine);
	}
	if (scanner.hasNextLine()) {
	    Assert.assertEquals("Error in line :" + ++i + " of file:" + outputFile, null, scanner.nextLine());
	}
	br.close();
    }

    private void executeEntryMethod() throws InstantiationException, IllegalAccessException, IllegalArgumentException {
	try {
	    Object object = getClazz().newInstance();
	    if (object instanceof Runnable) {
		Runnable runnable = (Runnable) object;
		runnable.run();
		return;
	    }
	    Method[] methods = getClazz().getMethods();
	    for (String methodName : getMethodsToExecute()) {
		for (Method method : methods) {
		    if (methodName.equals(method.getName())) {
			method.invoke(object, new Object[method.getParameterTypes().length]);
			return;
		    }
		}
	    }
	}
	catch (InvocationTargetException e) {
	    throw new RuntimeException(e.getCause());
	}
    }

    private void readInputFileContent(String inputFile) throws FileNotFoundException {
	InputStream inStream = new FileInputStream(getInputFile(inputFile));
	System.setIn(inStream);
    }

    private ByteArrayOutputStream interceptTheProgramOutput() {
	ByteArrayOutputStream testOutStream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(testOutStream));
	return testOutStream;
    }

    /**
     * Recursively loop the directory for the file
     * 
     * @param parentDir
     * @param fileToFetch
     * @return
     */
    private File getFile(File parentDir, final String fileToFetch) {
	final List<File> directories = new ArrayList<File>();
	File[] texFiles = parentDir.listFiles(new FileFilter() {
	    public boolean accept(File file) {
		if (file.isDirectory()) {
		    directories.add(file);
		    return false;
		}
		return file.getName().equals(fileToFetch);
	    }
	});
	if (texFiles.length > 0) {
	    return texFiles[0];
	}

	for (File dir : directories) {
	    File returnFile = getFile(dir, fileToFetch);
	    if (returnFile != null)
		return returnFile;
	}
	return null;
    }

    public void sendStringAndValidateString(String inputstring, String outputString) throws FileNotFoundException, IOException,
	    IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

	PrintStream consoleOutstream = System.out;
	InputStream consoleInStream = System.in;
	String programResult = null;

	try {
	    ByteArrayOutputStream testOutStream = interceptTheProgramOutput();

	    InputStream arrayInputStream = new ByteArrayInputStream(inputstring.getBytes("UTF-8"));
	    System.setIn(arrayInputStream);

	    executeEntryMethod();

	    programResult = testOutStream.toString();

	    Assert.assertEquals(outputString, programResult);
	}
	catch (ComparisonFailure exception) {
	    outputResultToConsole("input", consoleOutstream, programResult);
	    throw exception;
	}
	finally {
	    System.setIn(consoleInStream);
	    System.setOut(consoleOutstream);
	}
    }

}