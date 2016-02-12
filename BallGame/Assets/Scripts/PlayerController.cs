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
