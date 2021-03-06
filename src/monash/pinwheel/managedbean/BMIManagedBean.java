package monash.pinwheel.managedbean;

import java.sql.SQLException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.NamingException;

import monash.pinwheel.dao.BMIDbUtil;
import monash.pinwheel.entity.BMI;

// TODO: Auto-generated Javadoc
/**
 * The Class BMIManagedBean.
 */
@ManagedBean
@SessionScoped
public class BMIManagedBean {
	
	/**
	 * Instantiates a new BMI managed bean.
	 */
	public BMIManagedBean() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Adds the BMI record.
	 *
	 * @param bmiRecord the bmi record
	 * @return the string
	 */
	public String addBMIRecord(BMI bmiRecord) {
		try {
			// Insert all BMI records relate to the kid
			BMIDbUtil.getInstance().addBMI(bmiRecord);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String viewId = FacesContext.getCurrentInstance().getViewRoot().getViewId();
		return viewId + "?faces-redirect=true&id="+bmiRecord.getKidId();
	}
}
