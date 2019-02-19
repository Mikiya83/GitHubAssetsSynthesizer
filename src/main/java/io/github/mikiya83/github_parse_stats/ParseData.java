package io.github.mikiya83.github_parse_stats;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * GitHub Data synthesizer
 */
public class ParseData {

	/** Release max age option */
	private static final String MAX_AGE_OPTION = "max-age";

	/** Project name option */
	private static final String PROJECT_OPTION = "p";

	/** Ignore short release option */
	private static final String SHORT_RELEASE_OPTION = "ignore-short";

	/** Asset type option */
	private static final String TYPE_OPTION = "type";

	/** Format for GitHub API */
	private static String URL_GITHUB_REPO_DATA = "https://api.github.com/repos/%s/%s/releases";

	/** User name option */
	private static final String USER_OPTION = "u";

	/**
	 * Convert double number to String prettily.
	 */
	public static String formatDownloads(double d) {
		if (d == (long) d)
			return String.format("%d", (long) d);
		else
			return String.format("%s", d);
	}

	/**
	 * Get file extension
	 */
	public static String getExtension(String fileName) {
		String extension = "";

		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			extension = fileName.substring(i + 1);
		}
		return extension;
	}

	/**
	 * Use options to print data summaries about release's assets.
	 */
	public static void main(String[] args) {
		CommandLine commandlineOptions = null;
		if (args.length >= 4) {
			commandlineOptions = usePosixParser(args);
		} else {
			System.out.println("-- HELP --");
			printHelp(constructPosixOptions(), 80, "COMMAND HELP", System.lineSeparator() + "END OF HELP", 3, 5, true,
					System.out);
			System.exit(1);
		}
		if (!commandlineOptions.hasOption(USER_OPTION) || !commandlineOptions.hasOption(PROJECT_OPTION)) {
			printHelp(constructPosixOptions(), 80, "COMMAND HELP", "END OF HELP", 3, 5, true, System.out);
			System.exit(1);
		}

		String user = commandlineOptions.getOptionValue(USER_OPTION);
		String project = commandlineOptions.getOptionValue(PROJECT_OPTION);

		String fileype = null;
		if (commandlineOptions.hasOption(USER_OPTION)) {
			fileype = commandlineOptions.getOptionValue(TYPE_OPTION);
		}

		int maxAge = 0;
		if (commandlineOptions.hasOption(MAX_AGE_OPTION)
				&& commandlineOptions.getOptionValue(MAX_AGE_OPTION).matches("^\\d+$")) {
			maxAge = Integer.valueOf(commandlineOptions.getOptionValue(MAX_AGE_OPTION));
		}
		Boolean filterShort = commandlineOptions.hasOption(SHORT_RELEASE_OPTION);

		String urlText = String.format(URL_GITHUB_REPO_DATA, user, project);

		System.out.println("--- GitHub Project Data Synthesizer ---");
		System.out.println();
		System.out.println("- Analyzed Project : " + project + " -");
		System.out.println("- Analyzed User : " + user + " -");
		System.out.println();

		URL url = null;
		try {
			url = new URL(urlText);
		} catch (MalformedURLException exc) {
			exc.printStackTrace();
			return;
		}
		List<PrintData> printingData = new ArrayList<>();

		try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
			String inputLine;
			while ((inputLine = in.readLine()) != null) {

				// create ObjectMapper instance
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.findAndRegisterModules();
				objectMapper.registerModule(new Jdk8Module());
				objectMapper.registerModule(new JavaTimeModule());

				// read JSON like DOM Parser
				List<GitHubData> rootNode = objectMapper.readValue(inputLine, new TypeReference<List<GitHubData>>() {
				});

				int count = 0;
				LocalDateTime lastDate = null;

				// Compute data for each asset
				for (GitHubData gitHubData : rootNode) {
					for (AssetData asset : gitHubData.getAssets()) {
						if (fileype != null && !fileype.equalsIgnoreCase(getExtension(asset.getName()))) {
							continue;
						}
						LocalDateTime assetDate = LocalDateTime.ofInstant(gitHubData.getPublished_at(),
								ZoneId.systemDefault());
						long days = assetDate.until(LocalDateTime.now(), ChronoUnit.DAYS);
						if (maxAge > 0 && (days > maxAge)) {
							continue;
						}
						long activeDays = 0;
						float downStat = 0;
						if (lastDate != null) {
							activeDays = assetDate.until(lastDate, ChronoUnit.DAYS);
							if (activeDays > 0) {
								downStat = (float) asset.getDownload_count() / (float) activeDays;
							} else {
								downStat = asset.getDownload_count();
							}
						} else {
							downStat = (float) asset.getDownload_count() / (float) days;
						}
						lastDate = assetDate;

						// Filter for last release or more than 1 day of "last" state
						if (filterShort && activeDays < 1 && count != 0) {
							continue;
						}

						String state = (count == 0 ? "Last" : "Replaced after " + activeDays + " days");
						printingData.add(new PrintData(gitHubData.getName().trim(),
								assetDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), asset.getName(),
								asset.getDownload_count(), days, state, downStat));
						count++;
					}
				}
				if (count == 0) {
					System.err.println("No asset found in this project.");
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		// Format and print results
		printResults(printingData);
	}

	/**
	 * Construct and provide Posix-compatible Options.
	 * 
	 * @return Options expected from command-line of Posix form.
	 */
	private static Options constructPosixOptions() {
		final Options posixOptions = new Options();
		posixOptions.addOption(Option.builder(USER_OPTION).desc("User on GitHub").hasArg(true).required(true).build());
		posixOptions.addOption(
				Option.builder(PROJECT_OPTION).desc("Project on GitHub").hasArg(true).required(true).build());
		posixOptions.addOption(Option.builder(TYPE_OPTION).desc("Asset type").hasArg(true).required(false).build());
		posixOptions.addOption(Option.builder().longOpt(MAX_AGE_OPTION).desc("Maximum age (in days) for release")
				.hasArg(true).required(false).build());
		posixOptions.addOption(Option.builder().longOpt(SHORT_RELEASE_OPTION)
				.desc("Ignore release active less than 1 day").hasArg(false).required(false).build());
		return posixOptions;
	}

	/**
	 * Write "help" to the provided OutputStream.
	 */
	private static void printHelp(final Options options, final int printedRowWidth, final String header,
			final String footer, final int spacesBeforeOption, final int spacesBeforeOptionDescription,
			final boolean displayUsage, final OutputStream out) {
		final String commandLineSyntax = "java -jar github_assets_data.jar";
		final PrintWriter writer = new PrintWriter(out);
		final HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp(writer, printedRowWidth, commandLineSyntax, header, options, spacesBeforeOption,
				spacesBeforeOptionDescription, footer, displayUsage);
		writer.close();
	}

	/**
	 * Format and print results
	 */
	private static void printResults(List<PrintData> printingData) {

		int computeMaxName = 0;
		int computeMaxAssetFileName = 0;
		int computeMaxDownloads = 0;
		int computeMaxAge = 0;
		int computeMaxState = 0;

		// Compute width
		for (PrintData printData : printingData) {
			if (computeMaxName < printData.getName().length()) {
				computeMaxName = printData.getName().length();
			}
			if (computeMaxAssetFileName < printData.getAssetFileName().length()) {
				computeMaxAssetFileName = printData.getAssetFileName().length();
			}
			if (computeMaxDownloads < String.valueOf(printData.getDownloads()).length()) {
				computeMaxDownloads = String.valueOf(printData.getDownloads()).length();
			}
			if (computeMaxAge < String.valueOf(printData.getAge()).length()) {
				computeMaxAge = String.valueOf(printData.getAge()).length();
			}
			if (computeMaxState < printData.getState().length()) {
				computeMaxState = printData.getState().length();
			}
		}

		String printFormat = "%" + computeMaxName + "s | Tag date : %s | File : %" + computeMaxAssetFileName
				+ "s | Downloads : %" + computeMaxDownloads + "d | Since %" + computeMaxAge + "d days -- %"
				+ computeMaxState + "s | Downloads/day when last : %.2f";

		long totalDownloads = 0;
		long totalAssets = 0;

		for (PrintData printData : printingData) {
			totalDownloads += printData.getDownloads();
			totalAssets++;
			System.out.println(String.format(printFormat, printData.getName(), printData.getAssetDate(),
					printData.getAssetFileName(), printData.getDownloads(), printData.getAge(), printData.getState(),
					printData.getDownStat()));
		}

		System.out.println();
		System.out.println("--- Total assets : " + totalAssets + " - Total downloads : " + totalDownloads + " ---");
	}

	/**
	 * Apply Apache Commons CLI PosixParser to command-line arguments.
	 * 
	 * @param commandLineArguments
	 *            Command-line arguments to be processed with Posix-style parser.
	 */
	private static CommandLine usePosixParser(final String[] commandLineArguments) {
		final CommandLineParser cmdLinePosixParser = new DefaultParser();
		final Options posixOptions = constructPosixOptions();
		try {
			return cmdLinePosixParser.parse(posixOptions, commandLineArguments);
		} catch (org.apache.commons.cli.ParseException parseException) {
			System.err
					.println("Encountered exception while parsing using PosixParser:\n" + parseException.getMessage());
		}
		return null;
	}
}
