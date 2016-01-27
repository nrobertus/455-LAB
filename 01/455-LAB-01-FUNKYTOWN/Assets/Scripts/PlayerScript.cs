using UnityEngine;
using UnityEngine.UI;
using System.Collections;
using System.Diagnostics;

public class PlayerScript : MonoBehaviour
{

    public float speed;
    public Text countText;
    public Text winText;

    public Text timeText;

    private Rigidbody rb;
    private int count;

    Stopwatch stopwatch = new Stopwatch();

    void Start()
    {
        rb = GetComponent<Rigidbody>();
        count = 0;
        SetCountText();
        winText.text = "";
        timeText.text = "";
    }

    void FixedUpdate()
    {
        float moveHorizontal = Input.GetAxis("Horizontal");
        float moveVertical = Input.GetAxis("Vertical");

        Vector3 movement = new Vector3(moveHorizontal, 0.0f, moveVertical);

        rb.AddForce(movement * speed);

        timeText.text = stopwatch.Elapsed.ToString().Remove(stopwatch.Elapsed.ToString().Length - 5);
    }

    void OnTriggerEnter(Collider other)
    {
        if (other.gameObject.CompareTag("Pick Up"))
        {
            other.gameObject.SetActive(false);
            count = count + 1;
            SetCountText();
            if(count == 1)
            {
                stopwatch.Start();
            }
        }
    }

    void SetCountText()
    {
        countText.text = "Count: " + count.ToString();
        if (count >= 16)
        {
            stopwatch.Stop();
            winText.text = "You Win";
        }
    }
}