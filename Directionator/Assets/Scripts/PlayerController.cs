using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class PlayerController : MonoBehaviour {
    // Initialize global variables
    private Rigidbody rb;    
    public float speed;
    private int count;
    public Text countText;
    public Text winText;
    public Rigidbody pill;
    public Camera cam;

    // Starting functionality
    void Start()
    {
        // Initialize private variables
        rb = GetComponent<Rigidbody>();
        count = 0;
        setCountText();
        winText.text = "";
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
            if (z < 0 & x >= -range & x <= 0 ) { message = "Turn left"; }
            else if (z < 0 & x <= range & x > 0 ) { message = "Turn right"; }
            else { message = ("You're headed straight for it!"); }            
        }

        dispMessage(message);
    }

    // Handle collision with pickups
    void OnTriggerEnter(Collider other)
    {
        // If the object is one of our pickups
        if (other.gameObject.CompareTag("Pick Up"))
        {
            // Deactivate the pickup
            other.gameObject.SetActive(false);
            count++;
            setCountText();
        }
    }

    /// <summary>
    /// Display message to player
    /// </summary>
    void dispMessage(string s)
    {
        winText.text = s;
    }

    // This will set the count text
    void setCountText()
    {
        countText.text = "Pick ups collected: " + count.ToString();
        if(count == 12)
        {
            winText.text = "Congratulations! You Win!";
        }
    }
}
