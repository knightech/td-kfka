# K8s local deployment

### Pre-requisite
* Docker-For-Desktop installed
* Kubernetes running on Docker-For-Desktop
* Namespace 'td' from any previous run has been deleted:

```
kubectl delete namespace td
```

### Instructions
* Change directory to scripts/k8s
* Enter the following command to execute the Makefile:-

```
make
```

* The make consists of kubectl commands to create the environment in a namespace called 'td'
* It also runs required 'init' processes to set up the database and start the kafka connector
* Finally, it loads some dummy data onto kafka and then gets the created titles from the database

### Dashboard

* To view the deployed services in the K8s dashboard, install the dashboard by running the following:

```
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v1.10.1/src/deploy/recommended/kubernetes-dashboard.yaml
kubectl proxy &
```

* To create a dummy user that you will need to login with, run the following:-
```
kubectl apply -f admin.yaml
```

* Then to get the token you will need to login with, run the following:

```
kubectl -n kube-system describe secret $(kubectl -n kube-system get secret | grep admin-user | awk '{print $1}')
```

* Navigate to the following URL to login and view the 'td' namespace:-

```
http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/#!/overview?namespace=td
```