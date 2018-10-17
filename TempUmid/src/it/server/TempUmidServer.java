package it.server;

import org.eclipse.californium.core.CoapServer;

import it.resources.TempUmidResource;

public class TempUmidServer extends CoapServer {

	public static void main(String[] args) {
		TempUmidServer tempUmidServer = new TempUmidServer();
		
		//Instanciacao do recurso de leitura da temperatura e umidade e definicao do numero maximo de clientes concorrentes
		TempUmidResource tempUmidObs = new TempUmidResource("tempUmid-resource", 4);
		tempUmidObs.setObservable(true);
		tempUmidObs.getAttributes().setObservable();
		tempUmidServer.add(tempUmidObs);

		tempUmidServer.start();
	}

}
