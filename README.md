🏡 DecoraIA

Tu asistente inteligente para diseñar interiores con IA y Realidad Aumentada

DecoraIA es una aplicación móvil que combina Inteligencia Artificial, modelos 3D y Realidad Aumentada para ayudarte a visualizar ideas de decoración en tu espacio real, elegir estilos, guardar inspiraciones y chatear con una IA experta en diseño.

Esta app está diseñada pensando en usuarios  que quieren transformar su hogar sin necesidad de ser expertos en decoración.
Desde recomendaciones estéticas hasta rendering 3D con RA… esto es diseño del futuro.

✨ Características principales
Asistente de IA en Diseño

Chat interactivo para pedir ideas, moodboards y recomendaciones de decoración.

IA capaz de sugerir estilos, paletas y objetos según tus gustos.

xplorar Modelos 3D de Muebles y Decoración

Catálogo dinámico de modelos 3D organizados por estilo.

Modelos almacenados en Firebase Storage y cargados en tiempo real.

📱 Visualización en RA (Realidad Aumentada)

Selecciona un objeto y colócalo en tu propio espacio con ARCore.

Ajuste de escala y posición automática según superficie detectada.

Colocación realista con oclusión y seguimiento espacial.

🧾 Historial y Favoritos

Guarda decoraciones y conversaciones importantes.

Revisa ideas previas sin perder nada.

🔐 Autenticación y Seguridad

Login/Registro con Firebase Authentication

Gestión completa de perfil de usuario

☁️ Backend en Firebase

Firestore para datos estructurados

Storage para modelos .glb

Analytics para comportamiento de usuario

🛠️ Tecnologías utilizadas
Módulo	Herramienta
UI	Jetpack Compose + Material 3
Estado	ViewModel + Flow
Navigation	Navigation Compose
Backend	Firebase Auth + Firestore + Storage
IA	Google Gemini API
Render 3D + RA	ARCore + SceneView/Sceneform
Storage de modelos	GLB desde Firebase
Arquitectura	MVVM + Clean-ish modular approach
Lenguaje	Kotlin
🎯 Objetivo del proyecto

Demostrar la integración de:

✅ IA generativa en apps móviles
✅ Backend en la nube (Firebase)
✅ Gestión de contenido multimedia 3D
✅ Realidad aumentada para visualización de diseño
✅ UX moderna con Compose

Este proyecto cierra completamente el ciclo funcional:
Idea → Datos → Visualización 3D → RA + IA → Guardado y navegación real.

🧠 Estructura general del proyecto
app/
 ├── data/
 │   ├── models
 │   ├── repository
 ├── ui/
 │   ├── screens/
 │   ├── components/
 │   ├── nav/
 ├── utils/
 └── assets/
      └── modelos_3d

🚀 Cómo ejecutar el proyecto
Prerequisitos

Android Studio Ladybug o Flamingo+

SDK 34+

Dispositivo con soporte ARCore

Clonar el repo
git clone https://github.com/TU-USUARIO/DecoraIA.git
cd DecoraIA

Configurar API de Gemini

En local.properties agrega:

GEMINI_API_KEY=TU_API_KEY_AQUI

Ejecutar en dispositivo real

Link de descarga del APK 

https://drive.google.com/drive/folders/1x6hxvcQGiD5aOM2n0U0hI4s8GbugciNe?usp=drive_link

Dispositivo con Android + soporte ARCore:

👨‍💻 Profesor
Alejandro Franco Calderon

👨‍💻 Autores
Daniela Choconta 
Valery Martinez
Carol Arisa
Carlos Vargas
Juan De La Hoz



Si te gustó el proyecto, deja una estrellita ⭐ en el repo
¡y comparte la experiencia de diseñar con IA + AR!
