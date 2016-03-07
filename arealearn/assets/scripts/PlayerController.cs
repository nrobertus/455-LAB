using System.Collections;
using UnityEngine;
using Tango;
using UnityEngine.UI;
using System;

public class PlayerController : MonoBehaviour, ITangoLifecycle
{
    // Initialize global variables
    private TangoApplication m_tangoApplication;
    private Rigidbody rb;
    private AreaDescription area;    
    private int count;
    private bool loadSuccess = false;
    private bool localized = false;
    private string loadedMessage = "";

    public float speed;
    public Text winText;
    public Rigidbody pill;
    public Camera cam;
    
    // Starting functionality
    void Start()
    {
        // Initialize private variables
        rb = GetComponent<Rigidbody>();
        count = 0;
        winText.text = "";

        m_tangoApplication = FindObjectOfType<TangoApplication>();
        if (m_tangoApplication != null)
        {
            m_tangoApplication.Register(this);
            m_tangoApplication.RequestPermissions();
        }
    }

    // Fixed Update
    void FixedUpdate()
    {
        // Get axies of movements
        float moveHorizontal = Input.GetAxis("Horizontal");
        float moveVertical = Input.GetAxis("Vertical");

        // Create new vector3 for rigidbody
        Vector3 movement = new Vector3(moveHorizontal, 0.0f, moveVertical);

        // Add force to rigid body
        rb.AddForce(movement * speed);

        // Get output mesage
        string message = "";
        var relativePoint = cam.transform.InverseTransformPoint(pill.position);

        float x = relativePoint.x;
        float z = relativePoint.z;
        float range = 0.75f;

        if (x < -range)
            message = "Turn left";
        else if (x > range)
            message = "Turn right";
        else
        {
            if (z < 0 & x >= -range & x <= 0) { message = "Turn left"; }
            else if (z < 0 & x <= range & x > 0) { message = "Turn right"; }
            else { message = ("You're headed straight for it!"); }
        }
            
        dispMessage(loadedMessage + message);
    }

    public void OnTangoPermissions(bool permissionsGranted)
    {
        if (permissionsGranted)
        {
            AreaDescription[] list = AreaDescription.GetList();
            AreaDescription.Metadata mostRecentMetadata = null;
            if (list.Length > 0)
            {
                // Find and load the most recent Area Description
                area = list[0];
                mostRecentMetadata = area.GetMetadata();
                foreach (AreaDescription areaDescription in list)
                {
                    AreaDescription.Metadata metadata = areaDescription.GetMetadata();
                    if (metadata.m_name.Contains("nathanbrandon00"))
                    {
                        area = areaDescription;
                        break;
                    }
                }
                loadedMessage = "AREA LOADED" + Environment.NewLine;
                m_tangoApplication.Startup(area);               
                loadSuccess = true;
            }
        }
    }

    /// <summary>
    /// OnTangoPoseAvailable is called from Tango when a new Pose is available.
    /// </summary>
    /// <param name="pose">The new Tango pose.</param>
    public void OnTangoPoseAvailable(TangoPoseData pose)
    {
        if (pose.framePair.baseFrame == TangoEnums.TangoCoordinateFrameType.TANGO_COORDINATE_FRAME_AREA_DESCRIPTION
            && pose.framePair.targetFrame == TangoEnums.TangoCoordinateFrameType.TANGO_COORDINATE_FRAME_DEVICE)
        {
            if (pose.status_code == TangoEnums.TangoPoseStatusType.TANGO_POSE_VALID)
            {
                loadedMessage = loadedMessage + "LOCALIZED INNER" + Environment.NewLine;
                dispMessage(loadedMessage);
                localized = true;
            }
            else
            {
                loadedMessage = loadedMessage + "NOT LOCALIZED INNER" + Environment.NewLine;
                dispMessage(loadedMessage);                
            }
        }
    }

    /// <summary>
    /// Display message to player
    /// </summary>
    void dispMessage(string s)
    {
        winText.text = s;
    }

    public void OnTangoServiceConnected()
    {
        dispMessage(loadedMessage + "NOT LOCALIZED" + Environment.NewLine);
    }

    public void OnTangoServiceDisconnected()
    {

    }
}
