package openmods.calc.types.fraction;

import openmods.calc.IValuePrinter;
import openmods.calc.utils.config.ConfigProperty;
import org.apache.commons.lang3.math.Fraction;

public class FractionPrinter implements IValuePrinter<Fraction> {

	@ConfigProperty
	public boolean properFractions;

	@ConfigProperty
	public boolean expand;

	@Override
	public String str(Fraction value) {
		if (expand) return Double.toString(value.doubleValue());
		return properFractions? value.toProperString() : value.toString();
	}

	@Override
	public String repr(Fraction value) {
		return str(value);
	}

}
