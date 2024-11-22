package net.sourceforge.plantuml.tim.stdlib;

import static net.sourceforge.plantuml.tim.TimTestUtils.assertTimExpectedOutput;
import static net.sourceforge.plantuml.tim.TimTestUtils.assertTimExpectedOutputFromInput;

import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.IndicativeSentencesGeneration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import net.sourceforge.plantuml.tim.EaterException;
import net.sourceforge.plantuml.tim.TFunction;
import net.sourceforge.plantuml.tim.builtin.Hex2dec;

/**
 * Tests the builtin function.
 */
@IndicativeSentencesGeneration(separator = ": ", generator = ReplaceUnderscores.class)

class Hex2decTest {
	TFunction cut = new Hex2dec();
	final String cutName = "Hex2dec";

	@Test
	void Test_without_Param() throws EaterException {
		assertTimExpectedOutput(cut, "0");
	}

	@ParameterizedTest(name = "[{index}] " + cutName + "(''{0}'') = {1}")
	@CsvSource(nullValues = "null", value = {
			" 0    , 0 ",
			" 1    , 1 ",
			" a    , 10 ",
			" f    , 15 ",
			" 10   , 16 ",
			" ff   , 255 ",
			" ffff , 65535 ",
			" ' '  , 0 ",
			" g    , 0 ",
			" -g    , 0 ",
			" à    , 0 ",
			" -1    , -1 ",
			" -a    , -10 ",
	})
	void Test_with_String(String input, String expected) throws EaterException {
		assertTimExpectedOutputFromInput(cut, input, expected);
	}

	@ParameterizedTest(name = "[{index}] " + cutName + "({0}) = {1}")
	@CsvSource(nullValues = "null", value = {
			" 0    , 0 ",
			" 1    , 1 ",
			" 10   , 16 ",
			" -1    , -1 ",
	})
	void Test_with_Integer(Integer input, String expected) throws EaterException {
		assertTimExpectedOutputFromInput(cut, input, expected);
	}
}
