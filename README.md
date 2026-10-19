# GeoRegistro - Aplicación Android de Gestión de Incidencias

**GeoRegistro** es una aplicación móvil nativa desarrollada en Java para Android que permite registrar, geolocalizar y visualizar incidencias o reportes urbanos en tiempo real utilizando servicios de Firebase Cloud Firestore, Google Maps SDK y sensores del dispositivo.

---

## 🚀 Características Principales

* **Formulario de Registro de Incidencias:** Captura de título, descripción y categoría con validación de campos obligatorios.
* **Geolocalización GPS:** Obtención de coordenadas en tiempo real (`FusedLocationProviderClient`) con gestión de permisos en tiempo de ejecución.
* **Persistencia en la Nube:** Almacenamiento no relacional en Firebase Cloud Firestore con manejo de estados de red.
* **Visualización en Mapa:** Renderizado de puntos de incidencia mediante marcadores interactivos con Google Maps SDK.
* **Monitoreo de Sensores:** Lectura y presentación dinámica de datos del sensor Acelerómetro nativo.
* **Navegación Fluida:** Interfaz adaptativa construida con `BottomNavigationView` y `Fragments`.

---

## 🛠️ Tecnologías y Librerías

* **Lenguaje:** Java
* **SDK Mínimo:** API 26 (Android 8.0)
* **IDE:** Android Studio
* **Base de Datos:** Firebase Cloud Firestore (`com.google.firebase:firebase-firestore`)
* **Ubicación:** Google Play Services Location (`com.google.android.gms:play-services-location`)
* **Mapas:** Google Maps SDK for Android (`com.google.android.gms:play-services-maps`)

---

## 📋 Requisitos Previos e Instalación

1. Clonar el repositorio en tu máquina local: