// Copyright (c) 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020 Radek Lzicar & Ales Lanik
//
// This file is part of Nostalgia Emulator Framework.
//
// Nostalgia Emulator Framework is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nostalgia Emulator Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Nostalgia Emulator Framework. If not, see <http://www.gnu.org/licenses/>.

package com.nostalgiaemulators.framework.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import com.nostalgiaemulators.framework.ui.gamegallery.GameDescription;
import com.nostalgiaemulators.framework.ui.gamegallery.ZipRomFile;
import com.nostalgiaemulators.framework.utils.annotations.Column;
import com.nostalgiaemulators.framework.utils.annotations.ObjectFromOtherTable;
import com.nostalgiaemulators.framework.utils.annotations.Table;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String TAG = "com.nostalgiaemulators.framework.utils.DatabaseHelper";
	Class<?>[] classes = new Class<?>[] { GameDescription.class,
			ZipRomFile.class };
	private static int DB_VERSION_CODE = 22;

	private static DatabaseHelper instance;

	public synchronized static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context.getApplicationContext());
		}
		return instance;
	}

	private DatabaseHelper(Context context) {
		super(context, "db", null, DB_VERSION_CODE);
		for (Class<?> cls : classes) {
			classItems.put(cls, new ClassItem(cls));
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			for (Class<?> cls : classes) {
				String sql = getCreateSql(cls);
				db.execSQL(sql);
				Log.i(TAG, "sql:" + sql);
			}
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	@Override
	public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		if (oldVersion == 13 && newVersion == 21) {
			return;
		}

		removeTablesDB(db);
		onCreate(db);
	}

	private String getCreateSql(Class<?> cls) {
		ClassItem classItem = classItems.get(cls);
		String tableName = classItem.tableName;

		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE " + tableName + " (");

		for (Field field : cls.getDeclaredFields()) {
			Column column = (Column) field.getAnnotation(Column.class);
			if (column != null) {
				String cName = column.columnName();
				cName = cName.equals("") ? field.getName() : cName;

				Type type = field.getType();
				boolean supported = true;
				String dbType = "";

				if ((type == String.class) || ((Class<?>) type).isEnum()) {
					dbType = "TEXT";
				} else if (type == Integer.class || type == int.class
						|| type == Long.class || type == long.class
						|| type == Boolean.class || type == boolean.class) {
					dbType = "INTEGER";
				} else if (type == Float.class || type == float.class) {
					dbType = "REAL";
				} else {
					Log.e(TAG, "type " + type + " is not supported");
					supported = false;
				}

				if (supported) {
					sql.append(cName + " " + dbType + " ");

					if (column.isPrimaryKey()) {
						sql.append("PRIMARY KEY ");
					}
					if (!column.allowNull()) {
						sql.append("NOT NULL ");
					}

					if (column.unique()) {
						sql.append("UNIQUE ");
					}
					sql.append(",");
				}
			}

			ObjectFromOtherTable objectFromOtherTable = (ObjectFromOtherTable) field
					.getAnnotation(ObjectFromOtherTable.class);
			if (objectFromOtherTable != null) {
				Type fieldClass = field.getType();
				Table table2 = null;
				if (Collection.class.isAssignableFrom((Class<?>) fieldClass)) {
					Class<?> classType = getCollectionGenericClass(field);
					if (classType != null)
						table2 = (Table) classType.getAnnotation(Table.class);

				} else {
					table2 = (Table) field.getClass()
							.getAnnotation(Table.class);
				}

				if (table2 == null) {
					throw new RuntimeException(
							"Field "
									+ cls.getSimpleName()
									+ "."
									+ field.getName()
									+ " must refered to class with Table annotation"
									+ " or Collection with generic type with Table annotation");
				}
			}
		}

		sql.delete(sql.length() - 1, sql.length());

		sql.append(");");
		return sql.toString();
	}

	private Class<?> getCollectionGenericClass(Field field) {
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType type2 = (ParameterizedType) type;
			Type[] typeArguments = type2.getActualTypeArguments();
			return ((Class<?>) typeArguments[0]);
		} else {
			return null;
		}
	}

	private void removeTablesDB(SQLiteDatabase db) {
		for (Class<?> cls : classes) {
			Table table = (Table) cls.getAnnotation(Table.class);
			if (table != null) {
				String tableName = table.tableName();
				tableName = tableName.equals("") ? cls.getSimpleName()
						: tableName;
				db.execSQL("DROP TABLE IF EXISTS " + tableName);
				Log.i(TAG, "delete table " + tableName);
			} else {
				throw new RuntimeException("class " + cls.getName()
						+ " has not @Table annotation");
			}
		}
	}

	private void clearTablesDB(SQLiteDatabase db) {
		for (Class<?> cls : classes) {
			Table table = (Table) cls.getAnnotation(Table.class);
			if (table == null) {
				throw new RuntimeException("class " + cls.getName()
						+ " has not @Table annotation");
			}
			String tableName = table.tableName();
			tableName = tableName.equals("") ? cls.getSimpleName() : tableName;
			db.execSQL("DELETE FROM " + tableName);
			Log.i(TAG, "clear table " + tableName);
		}
	}

	public synchronized void clearDB() {
		SQLiteDatabase db = getWritableDatabase();
		try {
			clearTablesDB(db);
		} finally {
			db.close();
		}
	}

	public synchronized void updateObjToDb(Object obj, String[] fields) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			updateObjToDb(obj, db, fields);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public synchronized void insertObjToDb(Object obj) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			insertObjToDb(obj, db, null);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public synchronized void insertObjsToDb(List<Object> objs) {
		SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		try {
			for (Object obj : objs) {
				insertObjToDb(obj, db, null);
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
	}

	public synchronized int countObjsInDb(Class<?> cls, String where) {
		where = where == null ? "" : where;

		SQLiteDatabase db = getWritableDatabase();
		int count = -1;
		db.beginTransaction();
		try {
			count = countObjsInDb(cls, db, where);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
			db.close();
		}
		return count;
	}

	public synchronized <T> ArrayList<T> selectObjsFromDb(Class<T> cls) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, null, null, null, true);
		} finally {
			db.close();
		}
		return resultList;
	}

	public synchronized <T> ArrayList<T> selectObjsFromDb(Class<T> cls,
			boolean deep, String groupBy, String orderBy) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, null, groupBy, orderBy, deep);
		} finally {
			db.close();
		}
		return resultList;
	}

	public synchronized <T> ArrayList<T> selectObjsFromDb(Class<T> cls,
			boolean deep, String where, String groupBy, String orderBy) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, where, groupBy, orderBy,
					deep);
		} finally {
			db.close();
		}
		return resultList;
	}

	public synchronized <T> T selectObjFromDb(Class<T> cls, String where) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, where, null, null, true);
		} finally {
			db.close();
		}
		if (resultList.isEmpty()) {
			return null;
		} else {
			return resultList.get(0);
		}
	}

	public synchronized <T> T selectObjFromDb(Class<T> cls, String where,
			boolean deep) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, where, null, null, deep);
		} finally {
			db.close();
		}
		return resultList.get(0);
	}

	public synchronized <T> T selectObjFromDb(Class<T> cls, String where,
			String orderBy, boolean deep) {
		ArrayList<T> resultList = null;
		SQLiteDatabase db = getReadableDatabase();
		try {
			resultList = selectObjsFromDb(cls, db, where, null, orderBy, deep);
		} finally {
			db.close();
		}
		return resultList.get(0);
	}

	private synchronized void updateObjToDb(Object obj, SQLiteDatabase db,
			String[] fields) {
		Class<?> cls = obj.getClass();
		HashSet<String> fieldsSet = new HashSet<String>();
		if (fields != null)
			for (String f : fields)
				fieldsSet.add(f);
		ClassItem classItem = classItems.get(cls);

		if (classItem != null) {
			String tableName = classItem.tableName;

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE " + tableName + " SET ");

			StringBuffer wherePart = new StringBuffer();

			for (Field field : cls.getDeclaredFields()) {
				Column column = (Column) field.getAnnotation(Column.class);
				field.setAccessible(true);
				if (column != null) {
					String cName = column.columnName();
					cName = cName.equals("") ? field.getName() : cName;

					Object value;
					try {
						value = field.get(obj);
						if (column.isPrimaryKey()) {
							wherePart.append(cName + "=");
							if (value instanceof String) {
								wherePart.append("\"" + value + "\"");
							} else {
								wherePart.append(value);
							}
						} else {
							if (fields != null) {
								if (!fieldsSet.contains(cName)) {
									continue;
								}
							}
							sql.append(cName + "=");

							if (value instanceof String
									|| value.getClass().isEnum()) {
								sql.append("\"" + value + "\",");
							} else if (value instanceof Boolean) {
								sql.append(value.equals(Boolean.TRUE) ? 1 : 0);
								sql.append(",");
							} else {
								sql.append(value + ",");
							}
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "", e);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "", e);
					}
				}

				ObjectFromOtherTable objectFromOtherTable = (ObjectFromOtherTable) field
						.getAnnotation(ObjectFromOtherTable.class);
				if (objectFromOtherTable != null) {
					Type fieldClass = field.getType();
					if (fields != null) {
						if (!fieldsSet.contains(field.getName())) {
							continue;
						}
					}
					try {
						if (Collection.class
								.isAssignableFrom((Class<?>) fieldClass)) {

							Class<?> classType = getCollectionGenericClass(field);
							if (classType != null) {
								Collection<?> objs = (Collection<?>) field
										.get(obj);
								if (objs != null) {
									for (Object o : objs) {
										updateObjToDb(o, db, null);
									}
								}
							}

						} else {
							updateObjToDb(field.get(obj), db, null);
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "", e);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "", e);
					}
				}
			}
			sql.delete(sql.length() - 1, sql.length());
			sql.append(" WHERE " + wherePart.toString() + ";");
			Log.i(TAG, "sql:" + sql.toString());
			db.execSQL(sql.toString());

		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");
		}
	}

	private int countObjsInDb(Class<?> cls, SQLiteDatabase db, String where) {
		ClassItem classItem = classItems.get(cls);
		int count = -1;
		if (classItem != null) {
			String tableName = classItem.tableName;

			String query = "select count(*) from " + tableName + " " + where
					+ ";";

			Cursor c = null;
			try {
				c = db.rawQuery(query, null);
				c.moveToFirst();
				count = c.getInt(0);
			} finally {
				c.close();
			}
			return count;

		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");
		}
	}

	private void insertObjToDb(Object obj, SQLiteDatabase db,
			Pair<String, Long> idMapping) {
		Class<?> cls = obj.getClass();
		ClassItem classItem = classItems.get(cls);

		if (classItem != null) {
			String tableName = classItem.tableName;

			ContentValues cv = new ContentValues();
			ArrayList<Pair<ObjectFromOtherTable, Field>> foregeinFields = new ArrayList<Pair<ObjectFromOtherTable, Field>>();

			Field primaryKeyField = null;

			for (int i = 0; i < classItem.fields.length; i++) {
				Field field = classItem.fields[i];
				Column column = classItem.columns[i];
				if (column != null) {
					if (!column.isPrimaryKey()) {
						String cName = classItem.names[i];
						Object value;
						try {
							value = field.get(obj);
							Class<?> valueCls = value.getClass();

							if (idMapping != null
									&& idMapping.first.equals(cName)) {
								value = idMapping.second;
								field.setLong(obj, idMapping.second);
							}
							if (value instanceof String
									|| value.getClass().isEnum()) {
								cv.put(cName, (String) value);
							} else if (valueCls == Boolean.class
									|| valueCls == boolean.class) {
								cv.put(cName, value.equals(Boolean.TRUE) ? 1
										: 0);
							} else if (valueCls == Integer.class
									|| valueCls == int.class) {
								cv.put(cName, (Integer) value);
							} else if (valueCls == Long.class
									|| valueCls == long.class) {
								cv.put(cName, (Long) value);
							} else if (valueCls == Byte.class
									|| valueCls == byte.class) {
								cv.put(cName, (Byte) value);
							} else {
								throw new RuntimeException(value.getClass()
										+ " is not supported datatype");
							}
						} catch (IllegalArgumentException e) {
							Log.e(TAG, "", e);
						} catch (IllegalAccessException e) {
							Log.e(TAG, "", e);
						}
					} else {
						primaryKeyField = field;
					}
				}

				ObjectFromOtherTable objectFromOtherTable = (ObjectFromOtherTable) field
						.getAnnotation(ObjectFromOtherTable.class);
				if (objectFromOtherTable != null) {
					foregeinFields.add(new Pair<ObjectFromOtherTable, Field>(
							objectFromOtherTable, field));
				}
			}

			db.insert(tableName, null, cv);

			Cursor c = db.rawQuery("Select last_insert_rowid();", null);
			long lastId = 0;
			try {
				c.moveToFirst();
				lastId = c.getLong(0);
			} finally {
				c.close();
			}

			if (primaryKeyField != null) {
				try {
					primaryKeyField.set(obj, lastId);
				} catch (IllegalArgumentException e) {
					Log.e(TAG, "", e);
				} catch (IllegalAccessException e) {
					Log.e(TAG, "", e);
				}
			}

			for (Pair<ObjectFromOtherTable, Field> item : foregeinFields) {
				ObjectFromOtherTable objectFromOtherTable = item.first;
				Field field = item.second;

				if (objectFromOtherTable != null) {
					Type fieldClass = field.getType();
					try {
						if (Collection.class
								.isAssignableFrom((Class<?>) fieldClass)) {

							Pair<String, Long> idMap = new Pair<String, Long>(
									objectFromOtherTable.columnName(), lastId);

							Class<?> classType = getCollectionGenericClass(field);
							if (classType != null) {
								Collection<?> objs = (Collection<?>) field
										.get(obj);
								if (objs != null) {
									for (Object o : objs) {
										insertObjToDb(o, db, idMap);
									}
								}
							}

						} else {
							insertObjToDb(field.get(obj));
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "", e);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "", e);
					}
				}
			}

		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");
		}
	}

	public synchronized void deleteObjsFromDb(Class<?> cls, String where) {
		ClassItem classItem = classItems.get(cls);

		if (classItem != null) {
			String tableName = classItem.tableName;

			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM " + tableName + " " + where + ";");
			Log.i(TAG, "sql:" + sql.toString());
			SQLiteDatabase db = getWritableDatabase();
			try {
				db.execSQL(sql.toString());
			} finally {
				db.close();
			}
		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");

		}

	}

	public synchronized void deleteObjFromDb(Object obj) {
		Class<?> cls = obj.getClass();
		ClassItem classItem = classItems.get(cls);

		if (classItem != null) {
			String tableName = classItem.tableName;

			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM " + tableName + " WHERE ");

			for (Field field : cls.getDeclaredFields()) {
				Column column = (Column) field.getAnnotation(Column.class);
				field.setAccessible(true);
				if (column != null && column.isPrimaryKey()) {
					String cName = column.columnName();
					cName = cName.equals("") ? field.getName() : cName;
					sql.append(cName + "=");
					Object value;
					try {
						value = field.get(obj);
						if (value instanceof String) {
							sql.append("\"" + value + "\"");
						} else {
							sql.append(value);
						}

					} catch (IllegalArgumentException e) {
						Log.e(TAG, "", e);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "", e);
					}
				}
			}
			sql.append(";");
			Log.i(TAG, "sql:" + sql.toString());
			SQLiteDatabase db = getWritableDatabase();
			try {
				db.execSQL(sql.toString());
			} finally {
				db.close();
			}

		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");
		}
	}

	private class ClassItem {
		Field[] fields = null;
		Column[] columns = null;
		ObjectFromOtherTable[] objsFromObjectFromOtherTable;
		Table table = null;
		String tableName = "";
		Class<?>[] classes = null;
		String[] names = null;
		int primaryKeyIdx = -1;

		public ClassItem(Class<?> cls) {
			fields = cls.getDeclaredFields();
			columns = new Column[fields.length];
			objsFromObjectFromOtherTable = new ObjectFromOtherTable[fields.length];
			classes = new Class<?>[fields.length];
			names = new String[fields.length];
			table = (Table) cls.getAnnotation(Table.class);
			tableName = table.tableName();
			tableName = tableName.equals("") ? cls.getSimpleName() : tableName;
			for (int i = 0; i < fields.length; i++) {
				Field f = fields[i];
				f.setAccessible(true);
				Column column = columns[i] = (Column) f
						.getAnnotation(Column.class);
				if (column != null) {
					classes[i] = f.getType();
					names[i] = column.columnName();
					names[i] = names[i].equals("") ? f.getName() : names[i];
					if (column.isPrimaryKey()) {
						primaryKeyIdx = i;
					}
				}

				ObjectFromOtherTable objectFromOtherTable = (ObjectFromOtherTable) f
						.getAnnotation(ObjectFromOtherTable.class);
				if (objectFromOtherTable != null) {
					objsFromObjectFromOtherTable[i] = objectFromOtherTable;
				}

			}
		}
	}

	HashMap<Class<?>, ClassItem> classItems = new HashMap<Class<?>, DatabaseHelper.ClassItem>();

	private <T> ArrayList<T> selectObjsFromDb(Class<T> cls, SQLiteDatabase db,
			String where, String groupby, String orderBy, boolean deep) {
		ClassItem classItem = classItems.get(cls);

		if (classItem != null) {
			ArrayList<T> result = new ArrayList<T>();
			String tableName = classItem.tableName;
			StringBuffer sql = new StringBuffer();
			long time = System.currentTimeMillis();
			sql.append("SELECT * FROM " + tableName);
			if (where != null)
				sql.append(" " + where);
			if (groupby != null)
				sql.append(" " + groupby);
			if (orderBy != null)
				sql.append(" " + orderBy);
			sql.append(";");

			Cursor cursor = db.rawQuery(sql.toString(), null);
			try {
				while (cursor.moveToNext()) {
					try {
						T obj = cls.newInstance();
						int i = 0;
						String id = null;
						ArrayList<Pair<ObjectFromOtherTable, Field>> objsFromOtherTable = null;
						if (deep) {
							objsFromOtherTable = new ArrayList<Pair<ObjectFromOtherTable, Field>>();
						}

						for (int index = 0; index < classItem.fields.length; index++) {
							Field field = classItem.fields[index];
							Column column = classItem.columns[index];
							if (column != null) {
								Class<?> cl = classItem.classes[index];
								if (cl == String.class) {
									field.set(obj, cursor.getString(i));
									if (index == classItem.primaryKeyIdx)
										id = "\"" + cursor.getString(i) + "\"";
								} else if (cl.isEnum()) {
									@SuppressWarnings({ "unchecked", "rawtypes" })
									Enum enu = Enum.valueOf((Class<Enum>) cl,
											cursor.getString(i));
									field.set(obj, enu);
									if (index == classItem.primaryKeyIdx)
										id = "\"" + cursor.getString(i) + "\"";
								} else if (cl == int.class
										|| cl == Integer.class) {
									field.set(obj, cursor.getInt(i));
									if (index == classItem.primaryKeyIdx)
										id = cursor.getInt(i) + "";
								} else if (cl == long.class || cl == Long.class) {
									field.set(obj, cursor.getLong(i));
									if (index == classItem.primaryKeyIdx)
										id = cursor.getLong(i) + "";
								} else if (cl == boolean.class
										|| cl == Boolean.class) {
									field.set(obj, cursor.getInt(i) == 1);
								} else if (cl == float.class
										|| cl == Float.class) {
									field.set(obj, cursor.getFloat(i));
								}
								i++;
							}

							if (deep) {
								ObjectFromOtherTable objectFromOtherTable = classItem.objsFromObjectFromOtherTable[index];
								if (objectFromOtherTable != null) {
									objsFromOtherTable
											.add(new Pair<ObjectFromOtherTable, Field>(
													objectFromOtherTable, field));
								}
							}

						}

						if (deep) {
							for (Pair<ObjectFromOtherTable, Field> item : objsFromOtherTable) {
								Type fieldClass = item.second.getType();
								try {
									if (Collection.class
											.isAssignableFrom((Class<?>) fieldClass)) {

										Class<?> classType = getCollectionGenericClass(item.second);
										if (classType != null) {

											String whereS = "WHERE "
													+ item.first.columnName()
													+ "=" + id;

											Collection<?> items = selectObjsFromDb(
													classType, db, whereS,
													groupby, orderBy, deep);
											item.second.set(obj, items);
										}

									} else {
										Log.e(TAG, "Not Implemented yet");
									}
								} catch (IllegalArgumentException e) {
									Log.e(TAG, "", e);
								} catch (IllegalAccessException e) {
									Log.e(TAG, "", e);
								}
							}
						}

						result.add(obj);
					} catch (InstantiationException e) {
						Log.e(TAG, "", e);
					} catch (IllegalAccessException e) {
						Log.e(TAG, "", e);
					}
				}

			} finally {
				cursor.close();
			}
			Log.i(TAG, "total time:" + (System.currentTimeMillis() - time));

			return result;

		} else {
			throw new RuntimeException(
					"Wrong obj class (class must have annotation Table)");
		}
	}
}
