package com.furry.robot.test;

import java.io.BufferedReader;
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

/**
 * Extend the test class which needs to validate the tests against an input and output file
 */
public abstract class FileValidatingTestFixture {
    private Class<?> clazz;
    private String[] methodsToExecute = { "run", "main" };

    /**
     * Custom path of the test folder where input files are stored
     * 
     * @return
     */
    protected File getInputFile(final String fileName) {
	return getFile(new File(System.getProperty("user.dir")), fileName);
    }

    protected File getOutputFile(final String fileName) {
	return getFile(new File(System.getProperty("user.dir")), fileName);
    }

    protected String[] getMethodsToExecute() {
	return methodsToExecute;
    }

    public FileValidatingTestFixture(Class<?> clazz) {
	this.clazz = clazz;
    }

    private Class<?> getClazz() {
	return this.clazz;
    }

    PrintStream consoleOutstream = System.out;
    InputStream consoleInStream = System.in;

    protected void validateProgramOutputFromFile(String inputFile, String outputFile) throws IOException, InstantiationException,
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException {

	try {
	    ByteArrayOutputStream testOutStream = interceptTheProgramOutput();

	    readInputFileContent(inputFile);

	    executeMainMethod();

	    validateProgramOutputAgainstOutputfileContent(outputFile, testOutStream.toString());

	}
	finally {
	    System.setIn(consoleInStream);
	    System.setOut(consoleOutstream);
	}

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

    private void executeMainMethod() throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

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

    private void readInputFileContent(String inputFile) throws FileNotFoundException {
	InputStream inStream = new FileInputStream(getInputFile(inputFile));
	System.setIn(inStream);
    }

    private ByteArrayOutputStream interceptTheProgramOutput() {
	ByteArrayOutputStream testOutStream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(testOutStream));
	return testOutStream;
    }

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

}