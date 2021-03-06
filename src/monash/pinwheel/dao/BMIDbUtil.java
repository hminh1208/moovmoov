package monash.pinwheel.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import monash.pinwheel.entity.BMI;
import monash.pinwheel.entity.Kid;

// TODO: Auto-generated Javadoc
/**
 * The Class BMIDbUtil will contains all functions that relate to BMI Entity in
 * Database. This class is designed based on Singleton design pattern
 */
public class BMIDbUtil {

	/** The static class instance. */
	private static BMIDbUtil instance;

	/** The data source. */
	private DataSource dataSource;

	/** The jndi name. */
	private String jndiName = "java:comp/env/jdbc/child_obesity";

	/**
	 * Gets the singleton instance of the class.
	 *
	 * @return single instance of BMIDbUtil
	 * @throws NamingException
	 *             the naming exception
	 */
	public static BMIDbUtil getInstance() throws NamingException {
		if (instance == null) {
			instance = new BMIDbUtil();
		}

		return instance;
	}

	/**
	 * Instantiates a new BMIDbUtil instance.
	 *
	 * @throws NamingException
	 *             the naming exception
	 */
	private BMIDbUtil() throws NamingException {
		dataSource = getDataSource();
	}

	/**
	 * Gets the data source.
	 *
	 * @return the data source
	 * @throws NamingException
	 *             the naming exception
	 */
	private DataSource getDataSource() throws NamingException {
		Context context = new InitialContext();

		DataSource dataSource = (DataSource) context.lookup(jndiName);

		return dataSource;
	}

	/**
	 * Close connection to Database.
	 *
	 * @param conn
	 *            the connection to database
	 * @param st
	 *            the statement
	 * @param re
	 *            the result set
	 */
	private void close(Connection conn, Statement st, ResultSet re) {
		// TODO Auto-generated method stub
		try {
			if (re != null) {
				re.close();
			}

			if (st != null) {
				st.close();
			}

			if (conn != null) {
				conn.close();
			}

		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection to the Database
	 * @throws SQLException
	 *             the SQL exception
	 */
	private Connection getConnection() throws SQLException {
		// TODO Auto-generated method stub
		Connection theConn = dataSource.getConnection();

		return theConn;
	}

	/**
	 * Gets the kid BMI by their Id.
	 *
	 * @param kidId
	 *            the kid id
	 * @return the list of BMI records of that kid id
	 * @throws SQLException
	 *             the SQL exception
	 */
	public List<BMI> getKidBMIs(int kidId) throws SQLException {
		List<BMI> bmiRecords = new ArrayList<>();

		Connection conn = null;
		PreparedStatement myStmt = null;
		ResultSet result = null;

		try {
			conn = getConnection();
			String sql = "select * from bmirecord where kid_id = ? Order By input_date";
			myStmt = conn.prepareStatement(sql);
			myStmt.setInt(1, kidId);
			result = myStmt.executeQuery();

			while (result.next()) {
				bmiRecords.add(new BMI(result.getInt("id"), result.getInt("kid_id"), result.getFloat("weight"),
						result.getFloat("height"), result.getDate("input_date")));
			}
		} finally {
			close(conn, myStmt, result);
		}

		return bmiRecords;
	}

	/**
	 * Adds new the BMI.
	 *
	 * @param record
	 *            the BMI record
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean addBMI(BMI record) throws SQLException {

		Connection conn = null;
		PreparedStatement myStmt = null;

		if (checkExistRecord(record.getKidId(), record.getInputDate())) {
			return updateBMIRecord(record);
		}

		try {
			conn = getConnection();
			String sql = "Insert Into bmirecord(kid_id, weight, height, input_date) values(?, ?, ?, ?)";
			System.out.println("Mina" + sql);
			myStmt = conn.prepareStatement(sql);
			myStmt.setInt(1, record.getKidId());
			myStmt.setFloat(2, record.getWeight());
			myStmt.setFloat(3, record.getHeight());

			String[] date = record.getInputDate().split("/");
			if (date.length == 3) {
				record.setInputDate(date[2] + "-" + date[1] + "-" + date[0]);
			}
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date inputDate = null;
			try {
				System.out.println("test " + record.getInputDate());
				inputDate = new Date(df.parse(record.getInputDate()).getTime());
				myStmt.setDate(4, inputDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			myStmt.execute();
			System.out.println("test 1" + myStmt.toString());
			conn.commit();
			return true;
		} finally {
			close(conn, myStmt, null);
		}
	}

	/**
	 * Check exist BMI record by kidId and input date
	 *
	 * @param kidId
	 *            the kid id
	 * @param checkDate
	 *            the check input date
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean checkExistRecord(int kidId, String checkDate) throws SQLException {
		Connection conn = null;
		PreparedStatement myStmt = null;
		ResultSet result = null;

		try {
			conn = getConnection();
			String sql = "Select * from bmirecord where input_date = ? AND kid_id = ?";
			myStmt = conn.prepareStatement(sql);

			System.out.println("checkExistRecord" + checkDate);

			String[] date = checkDate.split("/");
			if (date.length == 3) {
				System.out.println("length " + date.length);
				checkDate = date[2] + "-" + date[1] + "-" + date[0];
			}
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date inputDate = null;
			try {
				inputDate = new Date(df.parse(checkDate).getTime());
				myStmt.setDate(1, inputDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			myStmt.setInt(2, kidId);

			if (myStmt.executeQuery().next()) {
				return true;
			} else {
				return false;
			}
		} finally {
			close(conn, myStmt, result);
		}
	}

	/**
	 * Update BMI record.
	 *
	 * @param record
	 *            the BMI record
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean updateBMIRecord(BMI record) throws SQLException {
		Connection conn = null;
		PreparedStatement myStmt = null;
		ResultSet result = null;

		try {
			conn = getConnection();
			String sql = "Update bmirecord set weight = ?, height = ? where input_date = ? AND kid_id = ?";
			myStmt = conn.prepareStatement(sql);

			String[] date = record.getInputDate().split("/");
			if (date.length == 3) {
				record.setInputDate(date[2] + "-" + date[1] + "-" + date[0]);
			}
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date inputDate = null;
			try {
				myStmt.setFloat(1, record.getWeight());
				myStmt.setFloat(2, record.getHeight());
				inputDate = new Date(df.parse(record.getInputDate()).getTime());
				myStmt.setDate(3, inputDate);
				myStmt.setInt(4, record.getKidId());
			} catch (ParseException e) {
				e.printStackTrace();
			}

			myStmt.execute();
			conn.commit();
			return true;
		} finally {
			close(conn, myStmt, result);
		}
	}

	/**
	 * Delete BMI record by kid id.
	 *
	 * @param kidId
	 *            the kid id
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean deleteBMIRecordByKidId(int kidId) throws SQLException {
		Connection conn = null;
		PreparedStatement myStmt = null;

		try {
			conn = getConnection();
			String sql = "Delete From bmirecord Where kid_id = ?";
			myStmt = conn.prepareStatement(sql);
			myStmt.setInt(1, kidId);

			myStmt.execute();
			conn.commit();
			return true;
		} finally {
			close(conn, myStmt, null);
		}
	}

	/**
	 * Delete BMI record by id.
	 *
	 * @param id
	 *            the BMI id
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean deleteBMIRecordById(int id) throws SQLException {
		Connection conn = null;
		PreparedStatement myStmt = null;

		try {
			conn = getConnection();
			String sql = "Delete From bmirecord Where id = ?";
			myStmt = conn.prepareStatement(sql);
			myStmt.setInt(1, id);

			myStmt.execute();
			conn.commit();
			return true;
		} finally {
			close(conn, myStmt, null);
		}
	}

	/**
	 * Delete BMI record by kid id and date.
	 *
	 * @param kidId
	 *            the kid id
	 * @param dateInput
	 *            the date input
	 * @return true, if successful
	 * @throws SQLException
	 *             the SQL exception
	 */
	public boolean deleteBMIRecordByKidIdAndDate(int kidId, String dateInput) throws SQLException {
		Connection conn = null;
		PreparedStatement myStmt = null;

		try {
			conn = getConnection();
			String sql = "Delete From bmirecord Where kid_id = ? and input_date = ?";
			myStmt = conn.prepareStatement(sql);
			myStmt.setInt(1, kidId);

			String[] date = dateInput.split("/");
			if (date.length == 3) {
				dateInput = date[2] + "-" + date[1] + "-" + date[0];
			}
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			Date inputDate = null;
			try {
				inputDate = new Date(df.parse(dateInput).getTime());
				myStmt.setDate(2, inputDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			myStmt.execute();
			conn.commit();
			return true;
		} finally {
			close(conn, myStmt, null);
		}
	}

	/**
	 * Calculate BMI.
	 *
	 * @param month
	 *            the month
	 * @param weight
	 *            the weight
	 * @param height
	 *            the height
	 * @param gender
	 *            the gender
	 * @return the status of calculation return 1, underweight. 2, normal. 3,
	 *         overweight and -1 if not found
	 * @throws SQLException
	 *             the SQL exception
	 */
	public int checkBMI(float month, float weight, float height, int gender) throws SQLException {

		Connection conn = null;
		PreparedStatement myStmt = null;
		ResultSet result = null;
		int status;

		try {
			conn = getConnection();
			String sql = "";

			if (gender == 0) {
				sql = "Select * From cleaned_boy_bmi Where age_months = ? ORDER BY value";
			} else {
				sql = "Select * From cleaned_girl_bmi Where age_months = ? ORDER BY value";
			}
			myStmt = conn.prepareStatement(sql);

			while (month % 1 != 0.5) {
				month += 0.1;
			}

			myStmt.setFloat(1, month);

			result = myStmt.executeQuery();

			String group = "";
			float currentBMI = weight / height / height * 10000;
			System.out.println(month + " " + currentBMI);

			while (result.next()) {
				System.out.println(result.getFloat("value") + " " + currentBMI);
				if (result.getFloat("value") <= currentBMI) {
					group = result.getString("group");
				} else {
					if (group.equals("")) {
						group = "underweight";
					}
					break;
				}
			}

			System.out.println(month + " " + currentBMI + " " + group);

			switch (group) {
			case "underweight":
				status = 1;
				break;
			case "normal":
				status = 2;
				break;
			case "overweight":
			case "obese":
				status = 3;
				break;
			default:
				status = -1;
			}
		} finally {
			close(conn, myStmt, null);
		}

		return status;

	}
}
