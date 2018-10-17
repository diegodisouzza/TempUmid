package it.client;

import java.util.Scanner;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.Request;

import com.pi4j.io.gpio.PinState;

import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.CoAP.Code;

public class TempUmidClient {

	public static void main(String[] args) {

		//Variavel para leitura de dados
		Scanner input = new Scanner(System.in);
		
		//Leitura do ip do servidor
		System.out.println("Insira o IP do servidor para conexão: ");
		String ip = input.nextLine();
		
		//Leitura do threshold de temperatura
		System.out.println("Insira o valor de threshold de temperatura: ");
		String temperatura = input.nextLine();
		
		//Leitura do threshold de umidade
		System.out.println("Agora insira o valor de threshold de umidade: ");
		String umidade = input.nextLine();
		
		//Criacao da variavel payload
		String payload = temperatura + " " + umidade;
		
		//Estabelece conexao com o servidor (ip do servidor passado como parametro) 
		CoapClient client = new CoapClient("coap://"+ip+":5683/tempUmid-resource");
		
		//Define o tipo de requisicao
		Request request = new Request(Code.GET, CoAP.Type.CON);
		
		//Configura o payload da requisicao
		request.setPayload(payload);
		
		//Envia a mensagem GET sincrona
		CoapResponse coapResp = client.advanced(request);
		
		//Escreve a resposta recebida do servidor
		System.out.println(respostaTempUmid(payload, coapResp));
	}
	
	/**
	 * Define a estrutura da mensagem a ser exibida no cliente
	 * @param threshold threshold de temperatura e umidade
	 * @param r resposta do servidor
	 * */
	public static String respostaTempUmid(String threshold, CoapResponse r) {
		//Atribui a variavel o valor de temperatura lido
		String temperatura = r.advanced().getPayloadString().split(" ")[0];
		//Atribui a variavel o valor de umidade lido
		String umidade = r.advanced().getPayloadString().split(" ")[1];
		
		//Atribui a variavel o status dos leds
		String piscando = r.advanced().getPayloadString().split(" ")[2].equals("true") ? "Piscando": "";
		String ledTemp = r.advanced().getPayloadString().split(" ")[3].equals("true") ? "Aceso" : "Apagado";
		String ledUmid = r.advanced().getPayloadString().split(" ")[4].equals("true") ? "Aceso" : "Apagado";
		
		//Define a estrutura da mensagem a ser exibida no cliente
		StringBuilder sb = new StringBuilder();
		sb.append("\n===============================================================\n");
		
		sb.append("Temperatura threshold: "+threshold.split(" ")[0]+" Temperatura no sensor: "+temperatura);
		sb.append("\nUmidade threshold: "+threshold.split(" ")[1]+" Umidade no sensor: "+umidade);
		sb.append("\nStatus dos LEDs: "+piscando);
		if(piscando.equals("")) {
			sb.append("\nLED de temperatura: "+ledTemp);
			sb.append("\nLED de umidade: "+ledUmid);
		}
		
		sb.append("\n===============================================================\n");
		
		return sb.toString();
	}
}
