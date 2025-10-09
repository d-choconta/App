using System.Collections.Generic;
using UnityEngine;
using UnityEngine.XR.ARFoundation;
using UnityEngine.XR.ARSubsystems;

public class TapToPlace : MonoBehaviour
{
    [Header("Prefab a colocar (Prefab azul)")]
    public GameObject furniturePrefab;

    [Header("Referencias AR (desde XR Origin)")]
    public ARRaycastManager raycastManager;
    public ARAnchorManager anchorManager;

    private readonly List<ARRaycastHit> hits = new List<ARRaycastHit>();

    void Awake()
    {
        if (!raycastManager) Debug.LogError("[TapToPlace] Falta ARRaycastManager (arrastra XR Origin).");
        if (!anchorManager) Debug.LogError("[TapToPlace] Falta ARAnchorManager (arrastra XR Origin).");
        if (!furniturePrefab) Debug.LogWarning("[TapToPlace] FurniturePrefab vacío (asigna un Prefab).");
    }

    void Update()
    {
#if UNITY_EDITOR
        // Click izquierdo en Editor (XR Simulation)
        if (Input.GetMouseButtonDown(0))
        {
            TryPlaceAt(Input.mousePosition, "EditorMouse");
        }
#else
        // Toque en dispositivo real
        if (Input.touchCount > 0)
        {
            var t = Input.GetTouch(0);
            if (t.phase == TouchPhase.Began)
                TryPlaceAt(t.position, "Touch");
        }
#endif
    }

    void TryPlaceAt(Vector2 screenPos, string source)
    {
        if (raycastManager == null) return;

        if (raycastManager.Raycast(screenPos, hits, TrackableType.PlaneWithinPolygon))
        {
            var pose = hits[0].pose;
            var plane = hits[0].trackable as ARPlane;

            var anchor = anchorManager != null && plane != null
                ? anchorManager.AttachAnchor(plane, pose)
                : null;

            if (anchor == null)
            {
                // Si no se pudo anclar, al menos instanciamos en la pose
                Debug.LogWarning("[TapToPlace] Anchor nulo. Instanciando sin ancla.");
                Instantiate(furniturePrefab, pose.position, pose.rotation);
            }
            else
            {
                Instantiate(furniturePrefab, pose.position, pose.rotation, anchor.transform);
            }
            Debug.Log($"[TapToPlace] Instanciado desde {source} en {pose.position}");
        }
        else
        {
            Debug.Log("[TapToPlace] Raycast SIN hit (¿hay planos simulados habilitados? ¿click en Game view?)");
        }
    }
}
