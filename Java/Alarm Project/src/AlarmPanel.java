// import gui library and packages
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;

// import date library and packages
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;

// import sound library and packages
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class AlarmPanel extends JPanel{
    // combo box values
    private final String[] hourOfDay = {"am", "pm"};
    
    // components
    private JLabel timeDisplay = new JLabel("", SwingConstants.CENTER);
    private JLabel dateDisplay = new JLabel("", SwingConstants.CENTER);
    
    private JFormattedTextField setHour = new JFormattedTextField();
    private JFormattedTextField setMin = new JFormattedTextField();
    private JFormattedTextField setSec =  new JFormattedTextField();
    
    private JComboBox<String> setHourOfDay = new JComboBox<String>(hourOfDay);
    
    private JButton setAlarm = new JButton("Set");
    private JButton stopAlarm = new JButton("Stop");
    private JButton snoozeAlarm = new JButton("Snooze");
    
    
    // listener
    private Listener listen = new Listener();
    
    // timer
    private Timer run = new Timer(0, listen);
    
    // date and time components
    private Calendar cal = Calendar.getInstance();
    
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    
    private DecimalFormat timeLayout = new DecimalFormat("00");
    private int fontSize = 20;
    
    // input time font settings
    private Font f1 = new Font("SansSerif", Font.PLAIN, 20);

    /** constructor */
    public AlarmPanel(){
	// create panels
	JPanel controlPanel = new JPanel();
	JPanel currentTimePanel = new JPanel();
	
	// set current time zone
	timeFormat.setTimeZone(TimeZone.getTimeZone("NZDT"));
	
	// start time
	run.start();

	// set size
	setPreferredSize(new Dimension(300, 300));
	
	// component settings 
	timeDisplay.setFont(new Font("san-serif", Font.PLAIN, fontSize+10));
	timeDisplay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
							BorderFactory.createLoweredBevelBorder()));
	
	dateDisplay.setFont(new Font("san-serif", Font.PLAIN, fontSize));
	
	setHourOfDay.setEditable(true);
	setHourOfDay.setPreferredSize(new Dimension(60, 30));
	setHourOfDay.setFont(f1);
	
	setAlarmTextFieldSettings(setHour);
	setAlarmTextFieldSettings(setMin);
	setAlarmTextFieldSettings(setSec);

	// add listeners to component
	setAlarm.addActionListener(listen);
	setAlarm.addKeyListener(listen);
	stopAlarm.addActionListener(listen);
	snoozeAlarm.addActionListener(listen);
	
	// adding focus listeners
	setTabFocusJFTF(setHour);
	setTabFocusJFTF(setMin);
	setTabFocusJFTF(setSec);
	
	setTabFocusJCB(setHourOfDay);
	
	// control panel settings
	controlPanel.setPreferredSize(new Dimension(200, 150));
	
	// current time panel settings
	currentTimePanel.setPreferredSize(new Dimension(200, 60));
	
	// add components to control panel
	controlPanel.add(setHour);
	controlPanel.add(setMin);
	controlPanel.add(setSec);
	controlPanel.add(setHourOfDay);
	
	controlPanel.add(setAlarm);
	controlPanel.add(stopAlarm);
	
	// add components to current time panel
	currentTimePanel.add(timeDisplay);
	
	// add to main window
	add(dateDisplay);
	add(currentTimePanel);
	add(controlPanel);
    }
    
    /** support method for setting alarm time text field format */
    private void setAlarmTextFieldSettings(JFormattedTextField component){
	component.setHorizontalAlignment(SwingConstants.CENTER);
	component.setPreferredSize(new Dimension(36, 30));
	component.setFont(f1);
	component.setText("00");
    }
    
    /** makes alarm time text fields focusable by tab */
    private void setTabFocusJFTF(JFormattedTextField component){
	component.addFocusListener(new FocusAdapter(){
	    public void focusGained(FocusEvent e){
		SwingUtilities.invokeLater(new Runnable(){
		    public void run(){
			component.selectAll();
		    }
		});
	    }
	});
    }
    
    /** makes alarm time combo box focusable by tab */
    private void setTabFocusJCB(JComboBox<String> component){
	Runnable doRun = new Runnable(){
	    public void run(){
		component.getEditor().setItem("am");
		component.getEditor().selectAll();
		component.requestFocus();
	    }
	};
	
	SwingUtilities.invokeLater(doRun);
    }
    
    /** inner listener class */
    private class Listener implements ActionListener, KeyListener{
	// time components data fields
	private int seconds, minutes, hours, alarmHour, alarmMin, amPm;
	private String hourOfDay, alarmHourOfDay;
	
	// alarm boolean data fields 
	private boolean alarmIsSet = false, initiateAlarm = false;
	
	// sound file data field
	private Clip soundFile;
	
	/** method updates current time */
	private void currentTime(){
	    // current time components
	    seconds = cal.get(Calendar.SECOND);
	    minutes = cal.get(Calendar.MINUTE);
	    hours = cal.get(Calendar.HOUR);
	    amPm = cal.get(Calendar.AM_PM);
		
	    // check if time of day is am or pm
	    switch(amPm){
	    case 0: // if amPm is 0, must be before noon
		hourOfDay = "am";
		break;
		
	    case 1: // if amPm is 1, must be after noon
		hourOfDay = "pm";
		break;
	    }
		
	    // if current hours is midnight hence 00
	    if(hours == 0){
		hours = 12;
	    }
	    
	    // display current time
	    timeDisplay.setText(timeLayout.format(hours)+":"+
				timeLayout.format(minutes)+":"+		
				timeLayout.format(seconds)+" "+				
				hourOfDay);
		
	    // update time object
	    cal = Calendar.getInstance();
	}
	
	/** method updates current date */
	private void currentDate(){
	    dateDisplay.setText(dateFormat.format(cal.getTime()));
	}
	
	/** check current time == alarm time */
	private void checkAlarmStatus(){
	    // check if alarm is set
	    if(alarmIsSet){
	 		    
		// play alarm time when current time == alarm time
		if(hours == alarmHour && minutes == alarmMin && 
				hourOfDay.equals(alarmHourOfDay)){
		    initiateAlarm = true;
		    
		    // don't check alarm time
		    alarmIsSet = false;
		}
	    }
	}
	
	// thread that allows alarm ring tone to play in the background
	private void playAlarmRingTone(){
	    // music files
	    Thread t = new Thread(new Runnable() {
		// run new thread
		public void run(){
		    // try play sound file
		    try{
			File file = new File("alarmMusic.wav");
			soundFile = AudioSystem.getClip();
			soundFile.open(AudioSystem.getAudioInputStream(file));
			soundFile.start();
			
		    } catch(Exception error){ // catch error if file cannot be accessed
			// display error message on popup window
			JOptionPane.showMessageDialog(null, error.getMessage());
		    }
		}    
	    });
	    
	    t.start(); // start new thread
	    initiateAlarm = false; // don't start thread again
	}
	
	public void actionPerformed(ActionEvent e){
	    // set alarm options
	    if(e.getSource() == setAlarm){
		alarmHour = Integer.parseInt(setHour.getText());
		alarmMin = Integer.parseInt(setMin.getText());
		alarmHourOfDay = (String)setHourOfDay.getSelectedItem();

		// indicates that alarm is set
		alarmIsSet = true;
	    }
	    
	    // keep time running
	    if(e.getSource() == run){
		checkAlarmStatus(); // compare current time with set alarm time
		currentTime(); // update current time
		currentDate(); // update current date
	    }
	    
	    // if alarm time == current time, play sound
	    if(initiateAlarm){
		playAlarmRingTone();
	    }

	    // stop the alarm
	    if(e.getSource() == stopAlarm){
		soundFile.stop();
	    }
	}
	
	public void keyPressed(KeyEvent e){
	    // if enter key is pressed
	    if(e.getKeyCode() == KeyEvent.VK_ENTER){
		// check alarm time to current time
		checkAlarmStatus();
	    }
	}
	
	// dormant methods
	public void keyTyped(KeyEvent e){}
	public void keyReleased(KeyEvent e){}
    } // inner class ends
}