using UnityEngine;
using System.Collections;

public class Rotater : MonoBehaviour {

    // Define count
    public int countReset;
    private int count = 0;
    private int rotate1 = 15;
    private int rotate2 = 30;
    private int rotate3 = 45;

	// Update is called once per frame
	void Update () {

        count++;
        if (count == countReset)
        {
            // Reset count and flip rotate direction
            count = 0;
            int temp = rotate1;
            rotate1 = -1 * rotate2;
            rotate2 = -1 * rotate3;
            rotate3 = -1 * temp;            
        }

        // Rotate the cubes
        transform.Rotate(new Vector3(rotate1, rotate2, rotate3) * Time.deltaTime);
       

	}
}
