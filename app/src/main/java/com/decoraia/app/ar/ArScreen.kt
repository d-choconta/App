package com.decoraia.app.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.ar.core.*
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.getUpdatedPlanes
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

@Composable
fun ArScreen(
    // Debe existir: app/src/main/assets/models/sillon.glb
    modelAssetPath: String = "models/sillon.glb"
) {
    // 1) Recursos principales de SceneView
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine)

    // Workaround recomendado por la lib (evita crash al destruir ARSceneView)
    val cameraNode = remember { ARSceneView.createARCameraNode(engine) }

    // 2) Estado del modelo cargado y plano
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }
    var planeRenderer by remember { mutableStateOf(true) }

    // 3) Cargar la instancia del modelo desde assets una sola vez
    LaunchedEffect(modelAssetPath) {
        modelNode = modelLoader.createModelInstance(modelAssetPath).let { instance ->
            ModelNode(modelInstance = instance).apply {
                // Escala inicial basada en ancho del modelo (ajusta si se ve grande/pequeño)
                scale = Scale(0.3f / size.x.coerceAtLeast(0.001f))
                rotation = Rotation() // sin rotación inicial
                isEditable = true     // permitir gestos (rotar/escala)
                isScaleEditable = true
                isRotationEditable = true
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            // Inyectamos recursos recordados
            engine = engine,
            view = view,
            cameraNode = cameraNode,
            modelLoader = modelLoader,
            childNodes = childNodes,

            // Mostrar/ocultar visualización de planos
            planeRenderer = planeRenderer,

            // Gestos básicos (tap/move/scale/rotate) si lo deseas
            onGestureListener = rememberOnGestureListener(),

            // Configuración de sesión ARCore (simple y compatible)
            sessionConfiguration = { _, config ->
                config.depthMode = Config.DepthMode.DISABLED
                config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                config.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            },

            // Cada frame: si aún no hay nada, crea un ancla en el centro y coloca el modelo
            onSessionUpdated = { session, frame ->
                // Si no hay nodos todavía y ya cargamos el modelo => crear ancla y añadir
                if (childNodes.isEmpty()) {
                    val anchorNode = frame.createCenterAnchorNode(engine)
                    val readyModel = modelNode
                    if (anchorNode != null && readyModel != null) {
                        anchorNode.addChildNode(readyModel)
                        childNodes.add(anchorNode)
                        planeRenderer = false // ya colocado, ocultamos planos
                    }
                }
            }
        )
    }
}

/**
 * Crea un AnchorNode en el centro del frame, sobre el primer plano detectado.
 */
private fun Frame.createCenterAnchorNode(engine: com.google.android.filament.Engine): AnchorNode? =
    getUpdatedPlanes().firstOrNull()
        ?.let { it.createAnchorOrNull(it.centerPose) }
        ?.let { anchor ->
            AnchorNode(engine, anchor).apply {
                isEditable = false
                isPositionEditable = false
                updateAnchorPose = false
            }
        }
