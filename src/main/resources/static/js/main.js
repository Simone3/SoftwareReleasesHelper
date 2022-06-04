

const doSomething = () => {

	fetch('/my-test-endpoint', {method:'POST',headers: {
      'Content-Type': 'application/json'
    }})
	  .then(data => console.log(data))
};

