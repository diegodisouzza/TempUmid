package it.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.ConcurrentCoapResource;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class TempUmidResource extends ConcurrentCoapResource {

	private GpioController gpio = GpioFactory.getInstance();
	//PINO 8 - led verde, atua de acordo com o estado da temperatura
	private GpioPinDigitalOutput ledTemp = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "MyLED", PinState.HIGH);
	//PINO 9 - led vermelho, atua de acordo com o estado da umidade
	private GpioPinDigitalOutput ledUmid = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "MyLED", PinState.HIGH);
						
	//variavel para armazenar os valores de temperatura e umidade
	private String leitura = "";
	//variavel para armazenar os status dos leds
	private String statusLed = "";
	//variaveis para controlar os status dos leds
	private Boolean piscando = false;
	private Boolean highTemp = false;
	private Boolean highUmid = false;

	//Construtor padrao da classe ConcurrentCoapResource
	public TempUmidResource(String name) {
		super(name);
	}

	//Construtor padrao da classe ConcurrentCoapResource com numero maximo de threads para clientes
	public TempUmidResource(String name, int threads) {
		super(name, threads);
	}

	@Override
	public void handleGET(CoapExchange exchange) {

		exchange.accept();

		String threshold = exchange.getRequestText();

		try {
			//Efetua a leitura da temperatura e umidade sensor
			leitura = leituraSensor();

			//Compara os valores de threshold com os valores de leitura
			matchTempUmid(threshold, leitura);
			
			//Armazena o status dos leds
			statusLed = piscando+" "+highTemp+" "+highUmid;
			
			//Configura o payload da resposta
			String payload = leitura + " " + statusLed;

			exchange.respond(ResponseCode.CONTENT, payload, MediaTypeRegistry.TEXT_PLAIN);
			
			setLED();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void matchTempUmid(String threshold, String leitura) throws InterruptedException {
		//Configura como false o status inicial dos leds
		piscando = false;
		highTemp = false;
		highUmid = false;
		//Determina o estado dos leds ao iniciar a requisicao, ou seja, apagados.
		ledTemp.low();
		ledUmid.low();

		Double tempT = Double.parseDouble(threshold.split(" ")[0]);
		Double umidT = Double.parseDouble(threshold.split(" ")[1]);

		Double tempL = Double.parseDouble(leitura.split(" ")[0]);
		Double umidL = Double.parseDouble(leitura.split(" ")[1]);

		//Define que os leds devem piscar caso temperatura e umidade lidas estejam 
		//abaixo ou abaixo da informada pelo cliente
		if ((tempL > tempT && umidL > umidT) || (tempL < tempT && umidL < umidT)) {
			piscando = true;			
		} else if (tempL > tempT) {
			//Acende o led verde caso a temperatura esteja acima da informada pelo cliente
			highTemp = true;
		} else if (umidL > umidT) {
			//Acende o led vermelho caso a umidade esteja acima da informada pelo cliente
			highUmid = true;
		}
	}

	private String leituraSensor() throws IOException, InterruptedException {
		//Responsavel pela integracao entre o programa python e a classe java
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec("python leitura.py");
		BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

		//Obtem a informacao passada pelo programa em python
		String leitura = br.readLine();
		br.close();
		process.waitFor();

		return leitura;
	}
	
	private void setLED() throws InterruptedException {
		//Atua sobre ambos os leds
		if(piscando) {
			Integer contador = 0;
			while(contador < 20) {
				ledTemp.toggle();
				ledUmid.toggle();
				Thread.sleep(1000);
				contador++;
			}
		}
		//Atua sobre o led verde (temperatura)
		else if (highTemp) {
			ledTemp.toggle();
		}
		//Atua sobre o led vermelho (umidade)
		else if (highUmid) {
			ledUmid.toggle();
		}
	}
}
