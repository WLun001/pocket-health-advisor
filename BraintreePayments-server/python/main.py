import braintree

braintree.Configuration.configure(
    braintree.Environment.Sandbox,
    'z4vty64pyh76xxp3',
    'j2k52vjh8bt74rw5',
    '2cda6f6e484a2a3319c22509a14e6015'
)

from flask import Flask, request
app = Flask(__name__)
@app.route("/client_token", methods=["GET"])
def client_token():
  return braintree.ClientToken.generate()

@app.route("/checkout", methods=["POST"])
def create_purchase():
  nonce = request.form["nonce"]
  amount = request.form["amount"]
  # Use payment method nonce here...

  result = braintree.Transaction.sale({
	    "amount": amount,
	    "payment_method_nonce": nonce,
	    "options": {
	      "submit_for_settlement": True
	    }
	})
  return str(result)
 
 
if __name__ == "__main__":
  app.run()
