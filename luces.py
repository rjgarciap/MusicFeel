#!/usr/bin/env python
import sys
import math
import random
import socket
import smbus # Acceso de Python al I2C bus
import time # Para el uso de retardos
# Definitions for the MCP23017 chip
addr = 0x20 #I2C direccion del bus del chip MCP23017
dirA = 0x01 #PortA I/O registro de direccionamiento de bits
dirB = 0x00 #PortB I/O registro de direccionamiento de bits
portA = 0x13 #PortA registro de datos
portB = 0x12 #PortB registro de datos

# Inicio del programa
print "Hello, Pi Matrix! (Press <Ctrl>+<C> to stop)"
bus = smbus.SMBus(1) #Usamos '1' para nuevas versiones de la Raspberry Pi y 0 para la primera version
bus.write_byte_data(addr, dirA, 0x00) #Todos a cero, por tanto todos los bits seran de salida en el puerto A
bus.write_byte_data(addr, dirB, 0x00) #Todos a cero, por tanto todos los bits seran de salida en el puerto B
bus.write_byte_data(addr, portB, 0x00) #Ponemos a nivel bajo las filas

#Funcion decodificadora de los mensajes, transforma la string en una lista con los datos
def RealData(energias):
	print energias
        array=[]
        dato=""
        for carac in energias:
                if(carac=="/"):
                        array.append(float(dato))
                	dato=""
                else:
                        dato+=str(carac)
        return array

#Funcion decodificadora de los mensaje, transforma la string en una lista 
#con las energias en dB (solo es correcto para los 19 primeros datos)
def DataToArray(energias):
        print energias
        array=[]
        dato=""
        for carac in energias:
                if(carac=="/"):
			if (float(dato)>0):
                        	array.append(int(round((5/2)*(20*math.log(float(dato))-120)/55)))
			dato=""
                else:
                        dato+=str(carac)
        return array

#Funcion que permite escribir mediante el bus en un registro
def Write  (register, value):
        bus.write_byte_data(addr, register, value)

#Funcion que permite encender una columna 'col' de LEDs de la matriz
def SetColumn (col):
        Write(portB, 0x00)
        Write(portA, 0x80>>col)
        time.sleep(1)

#Funcion que permite encender una fila 'row' de LEDs de la matriz
def SetRow (row):
        Write(portA, 0xFF)
        Write(portB,~(0x01<<row))
        time.sleep(1)

#Funcion que permite encender una columna 'col' de LEDs de la matriz
def WriteLED (row,col):
        Write(portA,col)
        Write(portB,row)

#Funcion que invierte el orden de los bits en el byte cambiando: bit0 por bit7, bit1 por bit6... 
def ReverseBits (byte):
    value = 0
    currentBit = 7
    for i in range(0,8):
        if byte & (1<<i):
            value |= (0x80>>i)
            currentBit -= 1
    return value

#Funcion para encender un patron determinado de LEDs
def SetPattern (rows, cols):
        WriteLED(~rows,ReverseBits(cols))

#Funcion usada para multiplexar 
def MultiplexDisplay(z,speed):
        for count in range(0,speed):
                for row in range(0, 8):
                        SetPattern(1<<row,z[row])

#Funcion para transformar un numero que representa n LEDs que estan encendidos de una columna, a hexadecimal(2, hasta dos bits => 7)
def IntToHex(row):
        hex=0
        i=0
        while(i<=row):
                hex+=2**i
                i+=1
        return hex
#Funcion que aplica IntToHex a una lista
def ArrayIntToHex(array):
        i=0
        for rows in array:
                array[i]=IntToHex(rows) 
                i+=1
        return array
#Funcion para el encendido de un numero alto de LEDs  de manera aleatoria
def RandomTot ():
        delay=0.03
        rowPattern = random.randint(200,255)
        colPattern = random.randint(200,255)
        SetPattern(rowPattern,colPattern)
        time.sleep(delay)

#Funcion para el encendido de un numero alto de LEDs  de manera aleatoria
def RandomGran ():
        delay=0.03
        rowPattern = random.randint(127,199)
        colPattern = random.randint(127,199)
        SetPattern(rowPattern,colPattern)
        time.sleep(delay)

#Funcion para el encendido de un numero medio de LEDs  de manera aleatoria
def RandomMed ():
        delay=0.03
        rowPattern = random.randint(50,85)
        colPattern = random.randint(50,85)
        SetPattern(rowPattern,colPattern)
        time.sleep(delay)

#Funcion para el encendido de un numero pequeno de LEDs  de manera aleatoria
def RandomPeq ():
        delay=0.03
        rowPattern = random.randint(0,32)
        colPattern = random.randint(0,32)
        SetPattern(rowPattern,colPattern)
        time.sleep(delay)

#Funcion para el encendido de unos pocos LEDs a la vez de manera aleatoria
def RandomPixels (numCycles=128):
    #puts random display patterns on the LED matrix
    delay = 0.02
    for count in range(0,numCycles):
        row = random.randint(0,7)
        col = random.randint(0,7)
        SetLED (row,col)
        time.sleep(delay)

#Funcion que enciende un LED en cada esquina
def PulseEsq4(delay):
        SetPattern(0x81,0x81)
        time.sleep(0.03)
        time.sleep(delay)

#Funcion que enciende 4 LEDs en cada esqunina
def PulseMed4(delay):
        SetPattern(0xC3,0xC3)
        time.sleep(0.03)
        time.sleep(delay)       

#Funcion que enciende 9 LEDs en cada esquina
def PulseAlt4(delay):
        SetPattern(0xE7,0xE7)
        time.sleep(0.03)
        time.sleep(delay)

#Funcion que ilumina la totalidad de los LEDs
def PulseTot4(delay):
        SetPattern(0xFF,0xFF)
        time.sleep(0.03)
        time.sleep(delay)

#Creamos el socket UDP
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

#Ponemos el socket escuchando en el puerto 8080
server_address = (sys.argv[2], 8080)
print >> sys.stderr, " starting up on %s port %s" % server_address
sock.bind(server_address)

while True:             

        data, addr = sock.recvfrom(1024)
        print >> sys.stderr, 'recieved %s bytes from %s ' % (len(data),addr)
        addr = 0x20 #I2C direccion del bus del chip MCP23017
        dirA = 0x01 #PortA I/O registro de direccionamiento de bits
        dirB = 0x00 #PortB I/O registro de direccionamiento de bits
        portA = 0x13 #PortA registro de datos
        portB = 0x12 #PortB registro de datos

        # En el caso de Ecualizador
        if(sys.argv[1]=="0"):
                arrayRepresenta=DataToArray(data)
                displayA=[IntToHex(arrayRepresenta[18]),IntToHex(arrayRepresenta[17]),IntToHex(arrayRepresenta[14]),IntToHex(arrayRepresenta[9]),IntToHex(arrayRepresenta[6]),IntToHex(arrayRepresenta[5]),IntToHex(arrayRepresenta[1]),IntToHex(arrayRepresenta[0])]
                MultiplexDisplay(displayA,7) 

        #En el caso de LEDs aleatorios           
        elif(sys.argv[1]=="1"):
                arrayRepresenta=RealData(data)
                indice=arrayRepresenta[20]
                if (indice <270):
                        RandomPeq()
                elif (indice>=270 and indice<277.5):
                        RandomMed()
                elif(indice>=277.5 and indice<285):
                        RandomGran()
                elif(indice>=285 and indice <300):
                        RandomTot()
                else:
                        SetPattern(0xFF, 0xFF)

        #En el caso de 4 esquinas
        else:   
                arrayRepresenta=RealData(data)
                indice=arrayRepresenta[20]
                if (indice < 270):
                        SetPattern(0x00,0x00)
                elif (indice>=270 and indice<277.5):
                        PulseEsq4(0)
        
                elif(indice>= 277.5 and indice<285):
                        PulseMed4(0)
                elif(indice>285 and indice < 300):
                        PulseAlt4(0)
                else:
                        PulseTot4(0)



