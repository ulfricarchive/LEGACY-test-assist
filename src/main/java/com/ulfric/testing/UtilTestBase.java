package com.ulfric.testing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import com.ulfric.testing.internal.EnumBuster;
import com.ulfric.verify.Verify;

@RunWith(JUnitPlatform.class)
@SuppressWarnings("unchecked")
public class UtilTestBase {

	private final Class<?> util = this.getClass().getAnnotation(Util.class).value();

	@Test
	void testIsEnum()
	{
		Verify.that(this.util).isEnum();
	}

	@Test
	public void test_instanceUtils_enum()
	{
		EnumBuster buster = new EnumBuster(this.util);

		Object VALUE = buster.make("VALUE");
		buster.addByValue((Enum) VALUE);

		try
		{
			Method values = this.util.getDeclaredMethod("values");

			values.invoke(null);

			Method valueof = this.util.getDeclaredMethod("valueOf", String.class);

			valueof.invoke(null, "VALUE");
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
	}

}