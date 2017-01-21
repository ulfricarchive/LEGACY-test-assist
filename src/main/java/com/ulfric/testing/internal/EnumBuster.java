package com.ulfric.testing.internal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import sun.reflect.ConstructorAccessor;

@SuppressWarnings("unchecked")
public class EnumBuster<E extends Enum<E>> {

	static final String ORDINAL_FIELD = "ordinal";
	private static final String VALUES_FIELD = "$VALUES";

	private static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	private final Class<E> clazz;
	private final Collection<Field> switchFields;

	private final Deque<Memento> undoStack = new LinkedList<>();

	public EnumBuster(Class<E> clazz, Class...switchUsers)
	{
		try
		{
			this.clazz = clazz;
			this.switchFields = findRelatedSwitchFields(switchUsers);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not create the class", e);
		}
	}

	public E make(String value)
	{
		return this.make(value, 0, EnumBuster.EMPTY_CLASS_ARRAY, EnumBuster.EMPTY_OBJECT_ARRAY);
	}

	private E make(String value, int ordinal, Class[] additionalTypes, Object[] additional)
	{
		try
		{
			this.undoStack.push(new Memento(this));

			ConstructorAccessor accessor = ReflectionHelper.findConstructorAccessor(additionalTypes, this.clazz);

			return ReflectionHelper.constructEnum(this.clazz, accessor, value, ordinal, additional);
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not create enum", e);
		}
	}

	public void addByValue(E e)
	{
		try
		{
			EnumHelper<E> helper = new EnumHelper<>(e, this.clazz);

			this.undoStack.push(new Memento(this));

			Field valuesField = this.findValuesField();

			E[] values = this.values();

			for (int i = 0; i < values.length; i++)
			{
				E value = values[i];
				if (value.name().equals(e.name()))
				{
					helper.setOrdinal(value.ordinal());
					values[i] = e;
					helper.replaceConstant();
					return;
				}
			}

			E[] newValues = Arrays.copyOf(values, values.length + 1);
			newValues[newValues.length - 1] = e;
			ReflectionHelper.setStaticFinalField(valuesField, newValues);

			int ordinal = newValues.length - 1;
			helper.setOrdinal(ordinal);
			this.addSwitchCase();
		}
		catch (Exception ex)
		{
			throw new IllegalArgumentException("Could not set the enum", ex);
		}
	}

	public void restore()
	{
		while (this.undo())
		{}
	}

	private boolean undo()
	{
		try
		{
			Memento memento = this.undoStack.poll();
			if (memento == null)
			{
			  return false;
			}

			memento.undo();

			return true;
		}
		catch (Exception e)
		{
			throw new IllegalStateException("Could not undo", e);
		}
	}

	private void addSwitchCase()
	{
		try
		{
			for (Field switchField : this.switchFields)
			{
				int[] switches = (int[]) switchField.get(null);

				switches = Arrays.copyOf(switches, switches.length + 1);

				ReflectionHelper.setStaticFinalField(switchField, switches);
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not fix switch", e);
		}
	}

	Field findValuesField() throws NoSuchFieldException
	{
		Field valuesField = this.clazz.getDeclaredField(VALUES_FIELD);
		valuesField.setAccessible(true);

		return valuesField;
	}

	private Collection<Field> findRelatedSwitchFields(Class[] switchUsers)
	{
		Collection<Field> result = new ArrayList<>();
		try
		{
			for (Class switchUser : switchUsers)
			{
				Class[] clazzes = switchUser.getDeclaredClasses();
				for (Class suspect : clazzes)
				{
					Field[] fields = suspect.getDeclaredFields();
					for (Field field : fields)
					{
						if (field.getName().startsWith("$SwitchMap$" + this.clazz.getSimpleName()))
						{
							field.setAccessible(true);
							result.add(field);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException("Could not fix switch", e);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	E[] values() throws NoSuchFieldException, IllegalAccessException
	{
		Field valuesField = this.findValuesField();
		return (E[]) valuesField.get(null);
	}

	Collection<Field> getSwitchFields()
	{
		return this.switchFields;
	}

	Class<E> getClazz()
	{
		return this.clazz;
	}

}