package com.kuka.generated.ioAccess;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.ioModel.AbstractIOGroup;
import com.kuka.roboticsAPI.ioModel.IOTypes;

/**
 * Automatically generated class to abstract I/O access to I/O group <b>BeckhoffIO</b>.<br>
 * <i>Please, do not modify!</i>
 * <p>
 * <b>I/O group description:</b><br>
 * ./.
 */
@Singleton
public class BeckhoffIOIOGroup extends AbstractIOGroup
{
	/**
	 * Constructor to create an instance of class 'BeckhoffIO'.<br>
	 * <i>This constructor is automatically generated. Please, do not modify!</i>
	 *
	 * @param controller
	 *            the controller, which has access to the I/O group 'BeckhoffIO'
	 */
	@Inject
	public BeckhoffIOIOGroup(Controller controller)
	{
		super(controller, "BeckhoffIO");

		addInput("In1", IOTypes.BOOLEAN, 1);
		addInput("In2", IOTypes.BOOLEAN, 1);
		addInput("In3", IOTypes.BOOLEAN, 1);
		addInput("In4", IOTypes.BOOLEAN, 1);
		addInput("In5", IOTypes.BOOLEAN, 1);
		addInput("In6", IOTypes.BOOLEAN, 1);
		addInput("In7", IOTypes.BOOLEAN, 1);
		addInput("In8", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out1", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out2", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out3", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out4", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out5", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out6", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out7", IOTypes.BOOLEAN, 1);
		addDigitalOutput("Out8", IOTypes.BOOLEAN, 1);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In1</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In1'
	 */
	public boolean getIn1()
	{
		return getBooleanIOValue("In1", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In2</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In2'
	 */
	public boolean getIn2()
	{
		return getBooleanIOValue("In2", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In3</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In3'
	 */
	public boolean getIn3()
	{
		return getBooleanIOValue("In3", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In4</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In4'
	 */
	public boolean getIn4()
	{
		return getBooleanIOValue("In4", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In5</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In5'
	 */
	public boolean getIn5()
	{
		return getBooleanIOValue("In5", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In6</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In6'
	 */
	public boolean getIn6()
	{
		return getBooleanIOValue("In6", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In7</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In7'
	 */
	public boolean getIn7()
	{
		return getBooleanIOValue("In7", false);
	}

	/**
	 * Gets the value of the <b>digital input '<i>In8</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital input
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital input 'In8'
	 */
	public boolean getIn8()
	{
		return getBooleanIOValue("In8", false);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out1</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out1'
	 */
	public boolean getOut1()
	{
		return getBooleanIOValue("Out1", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out1</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out1'
	 */
	public void setOut1(java.lang.Boolean value)
	{
		setDigitalOutput("Out1", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out2</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out2'
	 */
	public boolean getOut2()
	{
		return getBooleanIOValue("Out2", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out2</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out2'
	 */
	public void setOut2(java.lang.Boolean value)
	{
		setDigitalOutput("Out2", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out3</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out3'
	 */
	public boolean getOut3()
	{
		return getBooleanIOValue("Out3", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out3</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out3'
	 */
	public void setOut3(java.lang.Boolean value)
	{
		setDigitalOutput("Out3", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out4</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out4'
	 */
	public boolean getOut4()
	{
		return getBooleanIOValue("Out4", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out4</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out4'
	 */
	public void setOut4(java.lang.Boolean value)
	{
		setDigitalOutput("Out4", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out5</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out5'
	 */
	public boolean getOut5()
	{
		return getBooleanIOValue("Out5", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out5</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out5'
	 */
	public void setOut5(java.lang.Boolean value)
	{
		setDigitalOutput("Out5", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out6</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out6'
	 */
	public boolean getOut6()
	{
		return getBooleanIOValue("Out6", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out6</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out6'
	 */
	public void setOut6(java.lang.Boolean value)
	{
		setDigitalOutput("Out6", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out7</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out7'
	 */
	public boolean getOut7()
	{
		return getBooleanIOValue("Out7", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out7</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out7'
	 */
	public void setOut7(java.lang.Boolean value)
	{
		setDigitalOutput("Out7", value);
	}

	/**
	 * Gets the value of the <b>digital output '<i>Out8</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @return current value of the digital output 'Out8'
	 */
	public boolean getOut8()
	{
		return getBooleanIOValue("Out8", true);
	}

	/**
	 * Sets the value of the <b>digital output '<i>Out8</i>'</b>.<br>
	 * <i>This method is automatically generated. Please, do not modify!</i>
	 * <p>
	 * <b>I/O direction and type:</b><br>
	 * digital output
	 * <p>
	 * <b>User description of the I/O:</b><br>
	 * ./.
	 * <p>
	 * <b>Range of the I/O value:</b><br>
	 * [false; true]
	 *
	 * @param value
	 *            the value, which has to be written to the digital output 'Out8'
	 */
	public void setOut8(java.lang.Boolean value)
	{
		setDigitalOutput("Out8", value);
	}

}
