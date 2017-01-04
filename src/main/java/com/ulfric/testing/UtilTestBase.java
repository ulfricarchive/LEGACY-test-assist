package com.ulfric.testing;

import org.junit.jupiter.api.Test;

import com.ulfric.verify.Verify;

public class UtilTestBase {

	private final Class<? extends Enum<?>> util = this.getClass().getAnnotation(Util.class).value();

	@Test
	void testIsEnum()
	{
		Verify.that(this.util).isEnum();
	}

	@Test
	void testEnumHasNoConstants()
	{
		Verify.that(this.util.getEnumConstants()).isEmpty();
	}

}