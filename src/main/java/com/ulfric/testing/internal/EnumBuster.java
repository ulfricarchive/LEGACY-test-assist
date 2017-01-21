package com.ulfric.testing.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import sun.reflect.ConstructorAccessor;

@SuppressWarnings("unchecked")
public class EnumBuster<E extends Enum<E>> {

	static final String ORDINAL_FIELD = "ordinal";
	private static final String VALUES_FIELD = "$VALUES";

	private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private final Class<E> clazz;
	private final Collection<Field> switchFields;

	public EnumBuster(Class<E> clazz, Class...switchUsers)
	{
		this.clazz = clazz;
		this.switchFields = Sneaky.tryTo(() -> findRelatedSwitchFields(switchUsers));
	}

	public E make(String value)
	{
		return Sneaky.tryTo(() ->
		{
			ConstructorAccessor accessor = ReflectionHelper.findConstructorAccessor(EnumBuster.EMPTY_CLASS_ARRAY, this.clazz);

			return ReflectionHelper.constructEnum(this.clazz, accessor, value, 0, EnumBuster.EMPTY_OBJECT_ARRAY);
		});
	}

	public void addByValue(E e)
	{
		Sneaky.tryTo(() ->
		{
			E[] values = this.values();

			EnumHelper<E> helper = new EnumHelper<>(e, this.clazz);

			Field valuesField = this.findValuesField();

			if (this.ordinizeValue(e, values, helper))
			{
				return;
			}

			this.addNewValue(e, values, helper, valuesField);
		});
	}

	private boolean ordinizeValue(E e, E[] values, EnumHelper<E> helper)
	{
		return Sneaky.tryTo(() ->
		{
			for (int i = 0; i < values.length; i++)
			{
				E value = values[i];
				if (value.name().equals(e.name()))
				{
					helper.setOrdinal(value.ordinal());
					values[i] = e;
					helper.replaceConstant();
					return true;
				}
			}
			return false;
		});
	}

	private void addNewValue(E e, E[] values, EnumHelper<E> helper, Field valuesField)
	{
		Sneaky.tryTo(() ->
		{
			E[] newValues = Arrays.copyOf(values, values.length + 1);
			newValues[newValues.length - 1] = e;
			ReflectionHelper.setStaticFinalField(valuesField, newValues);

			int ordinal = newValues.length - 1;
			helper.setOrdinal(ordinal);
			this.addSwitchCase();
		});
	}

	private void addSwitchCase()
	{
		Sneaky.tryTo(() ->
		{
			for (Field switchField : this.switchFields)
			{
				int[] switches = (int[]) switchField.get(null);

				switches = Arrays.copyOf(switches, switches.length + 1);

				ReflectionHelper.setStaticFinalField(switchField, switches);
			}
		});
	}

	private Field findValuesField() throws NoSuchFieldException
	{
		Field valuesField = this.clazz.getDeclaredField(VALUES_FIELD);
		valuesField.setAccessible(true);

		return valuesField;
	}

	private Collection<Field> findRelatedSwitchFields(Class[] switchUsers)
	{
		Collection<Field> result = new ArrayList<>();

		Sneaky.tryTo(() ->
				this.searchSwitchUsers(switchUsers).forEach(field ->
				{
					if (field.getName().startsWith("$SwitchMap$" + this.clazz.getSimpleName()))
					{
						field.setAccessible(true);
						result.add(field);
					}
				})
		);

		return result;
	}

	private Stream<Field> searchSwitchUsers(Class[] switchUsers)
	{
		List<Field> results = new ArrayList<>();

		Stream.of(switchUsers).forEach(switchUser ->
				Stream.of(switchUser.getDeclaredClasses()).forEach(suspect ->
						Collections.addAll(results, suspect.getDeclaredFields())
				)
		);

		return results.stream();
	}

	@SuppressWarnings("unchecked")
	private E[] values() throws NoSuchFieldException, IllegalAccessException
	{
		Field valuesField = this.findValuesField();
		return (E[]) valuesField.get(null);
	}

}