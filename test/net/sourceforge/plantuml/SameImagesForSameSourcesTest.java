package net.sourceforge.plantuml;

import net.sourceforge.plantuml.code.ArobaseStringCompressor;
import net.sourceforge.plantuml.code.StringCompressor;
import net.sourceforge.plantuml.core.DiagramDescription;

import net.sourceforge.plantuml.klimt.drawing.svg.SvgGraphics;
import net.sourceforge.plantuml.preproc.Defines;
import net.sourceforge.plantuml.version.Version;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SameImagesForSameSourcesTest {

	private static final String SYSTEM_PROPERTY_LANGUAGE = "user.language";
	private static final String SYSTEM_PROPERTY_COUNTRY = "user.country";
	private static final String SYSTEM_PROPERTY_TIMEZONE = "user.timezone";

	private static String originalLanguage;
	private static String originalCountry;
	private static String originalTimezone;
	private static TimeZone originalDefaultTimeZone;

	@TempDir
	File tempDirectory;


	@BeforeAll
	public static void setUpForAll() {
		originalLanguage = System.getProperty(SYSTEM_PROPERTY_LANGUAGE);
		originalCountry = System.getProperty(SYSTEM_PROPERTY_COUNTRY);
		originalTimezone = System.getProperty(SYSTEM_PROPERTY_TIMEZONE);
		originalDefaultTimeZone = TimeZone.getDefault();
	}

	@AfterAll
	public static void tearDownForAll() {
		System.setProperty(SYSTEM_PROPERTY_LANGUAGE, originalLanguage);
		System.setProperty(SYSTEM_PROPERTY_COUNTRY, originalCountry);
		System.setProperty(SYSTEM_PROPERTY_TIMEZONE, originalTimezone);
		TimeZone.setDefault(originalDefaultTimeZone);
	}

	private static final String diagramExample = "@startuml\n" +
					"    interface List\n" +
					"    class ArrayList\n\n" +
					"    ArrayList --|> List\n" +
					"@enduml";

	@Test
	public void ensureSystemLanguageAndCountryAndTimeZoneDoNotAffectSvgResult() throws IOException {
		System.setProperty(SYSTEM_PROPERTY_LANGUAGE, "en");
		System.setProperty(SYSTEM_PROPERTY_COUNTRY, "GB");
		System.setProperty(SYSTEM_PROPERTY_TIMEZONE, "Europe/London");
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

		String svgCodeEn = plantUmlToSvg(diagramExample);

		assertNotNull(svgCodeEn);
		assertFalse(svgCodeEn.isBlank());

		System.setProperty(SYSTEM_PROPERTY_LANGUAGE, "zh");
		System.setProperty(SYSTEM_PROPERTY_COUNTRY, "HK");
		System.setProperty(SYSTEM_PROPERTY_TIMEZONE, "Asia/Hong_Kong");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));

		String svgCodeCh = plantUmlToSvg(diagramExample);

		assertNotNull(svgCodeCh);
		assertFalse(svgCodeCh.isBlank());

		assertEquals(svgCodeEn, svgCodeCh);
	}

	@Disabled
	@Test
	public void ensureCompileTimeTextDoesNotChangeWithTimeZone() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));

		String compileTimeUs = Version.compileTimeString();

		assertNotNull(compileTimeUs);

		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));

		String compileTimeFr = Version.compileTimeString();

		assertNotNull(compileTimeFr);

		assertEquals(compileTimeUs, compileTimeFr);
	}

	@Test
	public void ensureCheckMetadataDoesNotRecreateSvgIfSourceFileDoesNotChange() throws IOException {
		File pumlFile = new File(tempDirectory, "diagram.puml");
		Files.writeString(pumlFile.toPath(), diagramExample, StandardCharsets.UTF_8);

		System.setProperty(SYSTEM_PROPERTY_LANGUAGE, "en");
		System.setProperty(SYSTEM_PROPERTY_COUNTRY, "GB");
		System.setProperty(SYSTEM_PROPERTY_TIMEZONE, "Europe/London");
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"));

		File svgFile = plantUmlToSvg(pumlFile);
		BasicFileAttributes fileAttributes1 =
						Files.readAttributes(svgFile.toPath(), BasicFileAttributes.class);
		FileTime creationTime1 = fileAttributes1.creationTime();
		FileTime lastModifiedTime1 = fileAttributes1.lastModifiedTime();

		assertNotNull(svgFile);
		assertTrue(svgFile.exists());
		assertNotNull(fileAttributes1);
		assertNotNull(creationTime1);
		assertNotNull(lastModifiedTime1);

		System.setProperty(SYSTEM_PROPERTY_LANGUAGE, "zh");
		System.setProperty(SYSTEM_PROPERTY_COUNTRY, "HK");
		System.setProperty(SYSTEM_PROPERTY_TIMEZONE, "Asia/Hong_Kong");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));

		File svgFile2 = plantUmlToSvg(pumlFile);
		BasicFileAttributes fileAttributes2 =
						Files.readAttributes(svgFile2.toPath(), BasicFileAttributes.class);
		FileTime creationTime2 = fileAttributes2.creationTime();
		FileTime lastModifiedTime2 = fileAttributes2.lastModifiedTime();

		assertNotNull(svgFile2);
		assertTrue(svgFile2.exists());
		assertEquals(svgFile.toPath(), svgFile2.toPath());
		assertEquals(creationTime1, creationTime2);
		assertEquals(lastModifiedTime1, lastModifiedTime2);
	}

	@Test
	public void ensureSystemDependantMetaDataIsIgnoredWhenComparingPlantUmlSources() throws IOException {
		String svgCodeWithMetadata1 = diagramExample + "\n\n" +
						"PlantUML version 1.2023.3(Thu Mar 09 17:30:18 GMT 2023)\n" +
						"(GPL source distribution)\n" +
						"Java Runtime: OpenJDK Runtime Environment\n" +
						"JVM: OpenJDK 64-Bit Server VM\n" +
						"Default Encoding: UTF-8\n" +
						"Language: en\n" +
						"Country: GB";
		String svgCodeWithMetadata2 = diagramExample + "\n\n" +
						"PlantUML version 1.2023.3(Fri Mar 10 01:30:18 HKT 2023)\n" +
						"(GPL source distribution)\n" +
						"Java Runtime: OpenJDK Runtime Environment\n" +
						"JVM: OpenJDK 64-Bit Server VM\n" +
						"Default Encoding: UTF-8\n" +
						"Language: zh\n" +
						"Country: HK";
		String svgCodeWithMetadata3 = diagramExample + "\n\n" +
						"PlantUML version 1.2023.4 (Thu Mar 16 10:29:49 CEST 2023)\n" +
						"(EPL source distribution)\n" +
						"Java Runtime: Oracle JDK Runtime Environment\n" +
						"JVM: Oracle JDK 64-Bit Server VM\n" +
						"Default Encoding: UTF-16\n" +
						"Language: de\n" +
						"Country: DE";

		StringCompressor stringCompressor = new ArobaseStringCompressor();

		String currentSignature1 = SvgGraphics.getMetadataHex(svgCodeWithMetadata1);
		String relevantMetadataToCompare1 = stringCompressor.compress(svgCodeWithMetadata1);

		String currentSignature2 = SvgGraphics.getMetadataHex(svgCodeWithMetadata2);
		String relevantMetadataToCompare2 = stringCompressor.compress(svgCodeWithMetadata2);

		String currentSignature3 = SvgGraphics.getMetadataHex(svgCodeWithMetadata3);
		String relevantMetadataToCompare3 = stringCompressor.compress(svgCodeWithMetadata3);

		assertEquals(currentSignature1, currentSignature2);
		assertEquals(currentSignature1, currentSignature3);

		assertEquals(relevantMetadataToCompare1, relevantMetadataToCompare2);
		assertEquals(relevantMetadataToCompare1, relevantMetadataToCompare3);
	}

	@Disabled
	@Test
	public void ensureWhiteSpaceChangesInPlantUmlSourcesDoNotAffectMetadataComparison() throws IOException {
		String svgCodeWithMetadata1 = diagramExample + "\n\n" +
						"PlantUML version 1.2023.3(Thu Mar 09 17:30:18 GMT 2023)\n" +
						"(GPL source distribution)\n" +
						"Java Runtime: OpenJDK Runtime Environment\n" +
						"JVM: OpenJDK 64-Bit Server VM\n" +
						"Default Encoding: UTF-8\n" +
						"Language: en\n" +
						"Country: GB";

		String svgCodeWithMetadata2 = svgCodeWithMetadata1.replace(" ", "  \t");

		StringCompressor stringCompressor = new ArobaseStringCompressor();

		String currentSignature1 = SvgGraphics.getMetadataHex(svgCodeWithMetadata1);
		String relevantMetadataToCompare1 = stringCompressor.compress(svgCodeWithMetadata1);

		String currentSignature2 = SvgGraphics.getMetadataHex(svgCodeWithMetadata2);
		String relevantMetadataToCompare2 = stringCompressor.compress(svgCodeWithMetadata2);

		assertEquals(currentSignature1, currentSignature2);
		assertEquals(relevantMetadataToCompare1, relevantMetadataToCompare2);
	}

	private String plantUmlToSvg(String plantUmlSourceCode) throws IOException {
		String svgCode = null;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			SourceStringReader sourceReader = new SourceStringReader(plantUmlSourceCode);

			DiagramDescription svgDescription = sourceReader.outputImage(
							outputStream, new FileFormatOption(FileFormat.SVG));

			svgCode = outputStream.toString(StandardCharsets.UTF_8);
		}

		return svgCode;
	}

	public File plantUmlToSvg(File pumlSourceFile) throws IOException {
		File targetDir = pumlSourceFile.getParentFile();
		File targetSvgFile = null;
		SourceFileReader reader = new SourceFileReader(Defines.createWithFileName(pumlSourceFile),
						pumlSourceFile, targetDir, Collections.<String>emptyList(),
						"UTF-8", new FileFormatOption(FileFormat.SVG));

		// important not to create SVG files for unchanged PlantUML source files
		reader.setCheckMetadata(true);

		List<GeneratedImage> list = reader.getGeneratedImages();

		if (!list.isEmpty()) {
			GeneratedImage img = list.get(0);
			targetSvgFile = img.getPngFile();
		}

		return targetSvgFile;
	}

}