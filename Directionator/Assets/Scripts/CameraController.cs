using UnityEngine;
using System.Collections;

public class CameraController : MonoBehaviour {

    // Define global variables
    public GameObject playa;
    private Vector3 offset;

	// Use this for initialization
	void Start () {
        offset = transform.position - playa.transform.position;
	}
	
	// Update is called once per frame
	void LateUpdate () {
        transform.position = playa.transform.position + offset;
	}
}
