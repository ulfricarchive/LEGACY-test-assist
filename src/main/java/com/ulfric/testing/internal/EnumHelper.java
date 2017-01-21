package com.ulfric.testing.internal;

import java.lang.reflect.Field;

final class EnumHelper<E extends Enum<E>> {

	private final E e;
	private final Class<? extends Enum> enumClass;

	EnumHelper(E e, Class<E> enumClass)
	{
		this.e = e;
		this.enumClass = enumClass;
	}

	void replaceConstant() throws IllegalAccessException, NoSuchFieldException
	{
		Field[] fields = this.enumClass.getDeclaredFields();

		for (Field field : fields)
		{
			if (field.getName().equals(this.e.name()))
			{
				ReflectionHelper.setStaticFinalField(field, e);
			}
		}
	}

	void blankOutConstant() throws IllegalAccessException, NoSuchFieldException
	{
		Field[] fields = this.enumClass.getDeclaredFields();

		for (Field field: fields)
		{
			if (field.getName().equals(this.e.name()))
			{
				ReflectionHelper.setStaticFinalField(field, null);
			}
		}
	}

	void setOrdinal(int ordinal) throws NoSuchFieldException, IllegalAccessException
	{
		Field ordinalField = Enum.class.getDeclaredField(EnumBuster.ORDINAL_FIELD);
		ordinalField.setAccessible(true);
		ordinalField.set(this.e, ordinal);
	}

}
