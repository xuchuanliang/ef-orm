package jef.orm.multitable3;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import jef.database.DbClient;
import jef.database.QB;
import jef.database.test.DataSource;
import jef.database.test.DataSourceContext;
import jef.database.test.DatabaseInit;
import jef.database.test.JefJUnit4DatabaseTestRunner;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JefJUnit4DatabaseTestRunner.class)
@DataSourceContext({
		@DataSource(name = "oracle", url = "${oracle.url}", user = "${oracle.user}", password = "${oracle.password}"),
		@DataSource(name = "mysql", url = "${mysql.url}", user = "${mysql.user}", password = "${mysql.password}"),
		@DataSource(name = "postgresql", url = "${postgresql.url}", user = "${postgresql.user}", password = "${postgresql.password}"),
		@DataSource(name = "sqlserver", url = "${sqlserver.url}", user = "${sqlserver.user}", password = "${sqlserver.password}"),
		@DataSource(name = "derby", url = "${derby.url}"),
		@DataSource(name = "hsqldb", url = "${hsqldb.url}", user = "sa", password = ""),
		@DataSource(name = "sqlite", url = "${sqlite.url}")})
public class TestCase1 extends org.junit.Assert {

	private DbClient db;

	@BeforeClass
	public static void setUp() throws SQLException {
		// EntityEnhancer en=new EntityEnhancer();
		// en.enhance("jef.orm.multitable2.model");
	}

	@DatabaseInit
	public void prepareData() throws SQLException {
		db.createTable(Factor.class, Names.class);
		db.createTable(Factor2.class, Role.class, Sub1.class);
	}

	@Test
	public void case1() throws SQLException {
		db.select(QB.create(Factor.class));
	}

	@Test
	public void addData() throws SQLException {
		Role r1 = new Role("Role1");
		Role r2 = new Role("Role2");
		int id;
		// 插入多对多关系
		{
			Factor2 f = new Factor2();
			f.setRoles(new ArrayList<Role>());
			f.getRoles().add(r1);
			f.getRoles().add(r2);
			f.setSub1(new ArrayList<Sub1>());

			Sub1 s1 = new Sub1("Sub1-1");
			Sub1 s2 = new Sub1("Sub1-2");
			f.getSub1().add(s1);
			f.getSub1().add(s2);

			db.insertCascade(f);
			id = f.getId();
		}
		// 查询多对多关系
		System.out.println("===================查询多对多关系==================");
		Factor2 f2 = db.load(Factor2.class, id);
		assertEquals(2, f2.getRoles().size());
		assertEquals(2, f2.getSub1().size());

		//
		System.out.println("===================使用pageselect查询多对多关系==================");
		{
			List<Factor2> list = db.pageSelect(QB.create(Factor2.class), Factor2.class, 4).next();
			System.out.println(list);
			System.out.println(list.get(0).getRoles());
		}

		// 清除后重添加一个
		System.out.println("===================清除后重新添加一个==================");
		f2.getRoles().clear();
		f2.getRoles().add(r1);
		db.updateCascade(f2);
		f2 = db.load(Factor2.class, id);
		assertEquals(1, f2.getRoles().size());

		System.out.println("===================清空Role==================");
		f2.setRoles(null);
		db.updateCascade(f2);
		f2 = db.load(Factor2.class, id);
		assertEquals(0, f2.getRoles().size());

	}
}
