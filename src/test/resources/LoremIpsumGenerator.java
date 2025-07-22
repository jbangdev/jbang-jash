
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;


public class LoremIpsumGenerator {

	public static void main(String[] args) throws Exception {
		List<String> argList = Arrays.asList(args);
		// If "useInput" provided, then copy input to the output first, before generating lorem ipsum
		if (argList.size() > 0 && argList.get(0).equals("useInput") ) {
			try(var reader = new InputStreamReader(System.in); var bufferedReader = new BufferedReader(reader)) {
				bufferedReader.lines().forEach(System.out::println);
			}
		}
		
		// Any input has been written, so generate some nonesense that will exceed the pipe limit.
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 40; i++) {
			sb.append("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ")
			  .append("Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. ")
			  .append("Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.\n\n");
		}
		String paragraphs = sb.toString();
		System.out.println(paragraphs);
		
		// System.err.println("This should generate a test failure");
	}

}