# 🔍 Guide de Debugging avec les Logs NeverForget

## 📱 Filtrage des Logs dans Android Studio

### 1. Filtrage Principal - Tous les logs NeverForget
Dans **Logcat** d'Android Studio, utilise ce filtre :
```
tag:NeverForget
```

### 2. Filtres Spécialisés par Composant

#### 🗄️ Base de Données
```
tag:NeverForget-DB
```
**Affiche :** Opérations CRUD, requêtes SQL, migrations

#### 🔔 Notifications
```
tag:NeverForget-Notif
```
**Affiche :** Planifications, annulations, WorkManager

#### 🎯 Use Cases (Logique Métier)
```
tag:NeverForget-UC
```
**Affiche :** Validations de tâches, calculs de récurrence

#### 🖥️ ViewModels
```
tag:NeverForget-VM
```
**Affiche :** Changements d'état UI, interactions utilisateur

#### 🧭 Navigation
```
tag:NeverForget-Nav
```
**Affiche :** Transitions entre écrans

### 3. Filtrage par Niveau de Log

#### Erreurs Uniquement
```
tag:NeverForget level:error
```

#### Erreurs Critiques (toujours affichées)
```
tag:NeverForget-CRITICAL
```

#### Debug + Info + Warnings + Erreurs
```
tag:NeverForget level:debug
```

## 🛠️ Configuration dans Android Studio

### Étape 1 : Ouvrir Logcat
1. **View** → **Tool Windows** → **Logcat**
2. Ou raccourci : `Alt+6`

### Étape 2 : Créer un Filtre Personnalisé
1. Cliquer sur le **dropdown** à côté de "Show only selected application"
2. Sélectionner **"Edit Filter Configuration"**
3. Créer un nouveau filtre :
   - **Filter Name** : `NeverForget - Tous`
   - **Tag** : `NeverForget`
   - **Log Level** : `Debug`

### Étape 3 : Filtres Recommandés à Créer

#### Filtre 1 : Général
- **Nom** : `NeverForget - Général`
- **Tag** : `NeverForget`
- **Level** : `Debug`

#### Filtre 2 : Base de Données
- **Nom** : `NeverForget - Database`
- **Tag** : `NeverForget-DB`
- **Level** : `Debug`

#### Filtre 3 : Notifications
- **Nom** : `NeverForget - Notifications`
- **Tag** : `NeverForget-Notif`
- **Level** : `Debug`

#### Filtre 4 : Erreurs Seulement
- **Nom** : `NeverForget - Erreurs`
- **Tag** : `NeverForget`
- **Level** : `Error`

## 📋 Exemples de Logs à Surveiller

### ✅ Logs Normaux (Tout va bien)
```
D/NeverForget: NeverForget Application démarrée
D/NeverForget-UC: CompleteTaskUseCase.execute - taskId=123, date=2024-01-15
D/NeverForget-DB: updateTask - taskId=123, nextDueDate=2024-02-15
D/NeverForget-Notif: scheduleTaskNotification - Planification terminée
```

### ⚠️ Logs d'Attention
```
W/NeverForget-Notif: Date de notification déjà passée pour Détecteurs de fumée (2024-01-10)
```

### ❌ Logs d'Erreur
```
E/NeverForget: Erreur lors de la validation de la tâche 123
E/NeverForget-CRITICAL: Erreur critique de base de données
```

## 🔧 Commandes ADB (Alternative)

Si tu préfères la ligne de commande :

### Tous les logs NeverForget
```bash
adb logcat | grep "NeverForget"
```

### Logs en temps réel avec couleurs
```bash
adb logcat | grep --color=always "NeverForget"
```

### Sauvegarder les logs dans un fichier
```bash
adb logcat | grep "NeverForget" > neverforget_logs.txt
```

## 🎯 Debugging Efficace

### 1. Démarrage de l'App
Filtre : `tag:NeverForget`
Cherche : `Application démarrée`

### 2. Validation d'une Tâche
Filtre : `tag:NeverForget-UC`
Cherche : `CompleteTaskUseCase.execute`

### 3. Problèmes de Notifications
Filtre : `tag:NeverForget-Notif`
Cherche : `scheduleTaskNotification` ou `ERROR`

### 4. Erreurs de Base de Données
Filtre : `tag:NeverForget-DB`
Cherche : `ERROR` ou `Exception`

## 💡 Conseils Pro

1. **Utilise plusieurs onglets** Logcat avec différents filtres
2. **Pause/Resume** les logs pendant les tests pour ne pas perdre d'infos
3. **Clear** régulièrement pour éviter la surcharge
4. **Copie les logs d'erreur** pour les analyser plus tard

---

**Avec ces filtres, tu auras une vision claire de ce qui se passe dans l'app ! 🎯**