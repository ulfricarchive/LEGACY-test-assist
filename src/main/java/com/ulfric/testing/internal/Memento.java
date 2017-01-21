package com.ulfric.testing.internal;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

final class Memento<E extends Enum<E>> {

	private final EnumBuster<E> enumBuster;
	private final E[] values;
	private final Map<Field, int[]> savedSwitchFieldValues = new HashMap<>();

	Memento(EnumBuster<E> enumBuster) throws IllegalAccessException
	{
		this.enumBuster = enumBuster;
		try
		{
			this.values = this.enumBuster.values().clone();
			for (Field switchField : this.enumBuster.getSwitchFields())
			{
				int[] switchArray = (int[]) switchField.get(null);
				this.savedSwitchFieldValues.put(switchField, switchArray.clone());
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not create the class", e);
		}
	}

	void undo() throws NoSuchFieldException, IllegalAccessException
	{
		Field valuesField = this.enumBuster.findValuesField();
		ReflectionHelper.setStaticFinalField(valuesField, this.values);

		for (int i = 0; i < this.values.length; i++)
		{
			new EnumHelper<>(this.values[i], this.enumBuster.getClazz()).setOrdinal(i);
		}

		Map<String, E> valuesMap = new HashMap<>();

		for (E e : this.values)
		{
			valuesMap.put(e.name(), e);
		}

		Field[] constantEnumFields = this.enumBuster.getClazz().getDeclaredFields();

		for (Field constantEnumField: constantEnumFields)
		{
			E en = valuesMap.get(constantEnumField.getName());

			if (en != null)
			{
				ReflectionHelper.setStaticFinalField(constantEnumField, en);
			}
		}

		for (Map.Entry<Field, int[]> entry : this.savedSwitchFieldValues.entrySet())
		{
			Field field = entry.getKey();
			int[] mappings = entry.getValue();
			ReflectionHelper.setStaticFinalField(field, mappings);
		}
	}

}
